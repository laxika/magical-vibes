package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Decommission;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkySkiff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaughtInTheBrights.class, GrizzlyBears.class, SkySkiff.class, FountainOfYouth.class, Decommission.class})
class CaughtInTheBrightsTest extends BaseCardTest {

    @Test
    @DisplayName("Caught in the Brights prevents the enchanted creature from attacking")
    void preventsAttacking() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachTo(creature, player2);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Caught in the Brights prevents the enchanted creature from blocking")
    void preventsBlocking() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attachTo(blocker, player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("A Vehicle you control attacking exiles the enchanted creature")
    void vehicleAttackExilesEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        attachTo(enchanted, player1);
        Permanent vehicle = addCreatureReady(player1, new SkySkiff());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(vehicle)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A non-Vehicle creature attacking does not exile the enchanted creature")
    void nonVehicleAttackDoesNotExile() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        attachTo(enchanted, player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Caught in the Brights can target only a creature")
    void targetsOnlyCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new CaughtInTheBrights()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving the Aura spell enchants its target and prevents attacking")
    void auraSpellResolvesOntoCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CaughtInTheBrights()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Caught in the Brights").getAttachedTo())
                .isEqualTo(creature.getId());
        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An opponent's Vehicle attacking does not trigger the Aura")
    void opposingVehicleDoesNotExile() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        attachTo(enchanted, player1);
        addCreatureReady(player2, new SkySkiff());
        addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enchanted);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroying the Aura in response does not stop its exile trigger")
    void exileUsesLastAttachmentWhenAuraIsDestroyed() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attachTo(enchanted, player1);
        Permanent vehicle = addCreatureReady(player1, new SkySkiff());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Decommission()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(vehicle))));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);

        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Caught in the Brights");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchanted.getCard());
    }

    private Permanent attachTo(Permanent creature, Player controller) {
        Permanent aura = new Permanent(new CaughtInTheBrights());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
