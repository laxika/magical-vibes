package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DeathWind;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.n.NaturalEnd;
import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.cards.v.VesselOfEndlessRest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnhallowedPact.class, MoorlandInquisitor.class, DeathWind.class,
        NaturalEnd.class, PillarOfFlame.class, PlanarCleansing.class, VesselOfEndlessRest.class})
class UnhallowedPactTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, it returns under the Aura controller's control")
    void returnsUnderAuraControllersControl() {
        Permanent creature = addCreatureReady(player2, new MoorlandInquisitor());
        Card creatureCard = creature.getCard();

        castUnhallowedPact(player1, creature);
        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The Aura's own creature comes back under its controller too")
    void returnsOwnCreature() {
        Permanent creature = addCreatureReady(player1, new MoorlandInquisitor());
        Card creatureCard = creature.getCard();

        castUnhallowedPact(player1, creature);
        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Unhallowed Pact goes to the graveyard when the enchanted creature dies")
    void auraGoesToGraveyardOnDeath() {
        Permanent creature = addCreatureReady(player2, new MoorlandInquisitor());

        castUnhallowedPact(player1, creature);
        killCreature(creature);

        harness.assertInGraveyard(player1, "Unhallowed Pact");
        harness.assertNotOnBattlefield(player1, "Unhallowed Pact");
    }

    @Test
    @DisplayName("Unhallowed Pact cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new VesselOfEndlessRest());

        harness.setHand(player1, List.of(new UnhallowedPact()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The death trigger survives the Aura dying simultaneously with its creature")
    void returnsCreatureAfterSimultaneousAuraDeath() {
        Permanent creature = addCreatureReady(player1, new MoorlandInquisitor());
        castUnhallowedPact(player1, creature);
        Permanent aura = findPermanent(player1, "Unhallowed Pact");
        // Battlefield list order must not affect simultaneous death triggers.
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerBattlefields.get(player1.getId()).addFirst(aura);

        harness.setHand(player1, List.of(new PlanarCleansing()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Moorland Inquisitor");
        harness.assertInGraveyard(player1, "Unhallowed Pact");
    }

    @Test
    @DisplayName("Destroying the Aura before the creature dies prevents its return")
    void doesNotReturnCreatureAfterAuraWasDestroyed() {
        Permanent creature = addCreatureReady(player2, new MoorlandInquisitor());
        castUnhallowedPact(player1, creature);
        harness.setHand(player1, List.of(new NaturalEnd()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Unhallowed Pact"));

        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, 2, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Moorland Inquisitor");
        harness.assertNotOnBattlefield(player1, "Moorland Inquisitor");
    }

    @Test
    @DisplayName("A creature exiled instead of dying does not trigger Unhallowed Pact")
    void exileReplacementDoesNotReturnCreature() {
        Permanent creature = addCreatureReady(player2, new MoorlandInquisitor());
        castUnhallowedPact(player1, creature);
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(creature.getCard().getId()));
        harness.assertNotOnBattlefield(player1, "Moorland Inquisitor");
        harness.assertNotInGraveyard(player2, "Moorland Inquisitor");
    }

    @Test
    @DisplayName("The returned creature is a new permanent and goes to its owner's graveyard on a later death")
    void returnedCreatureDiesToOwnersGraveyard() {
        Permanent creature = addCreatureReady(player2, new MoorlandInquisitor());
        castUnhallowedPact(player1, creature);
        killCreature(creature);
        Permanent returned = findPermanent(player1, "Moorland Inquisitor");
        assertThat(returned.getCard().getId()).isEqualTo(creature.getCard().getId());
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();

        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, 2, returned.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Moorland Inquisitor");
        harness.assertNotInGraveyard(player1, "Moorland Inquisitor");
        harness.assertNotOnBattlefield(player1, "Moorland Inquisitor");
    }

    private void castUnhallowedPact(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new UnhallowedPact()));
        harness.addMana(controller, ManaColor.BLACK, 3);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, 2, creature.getId());
        harness.passBothPriorities(); // resolve Death Wind; the creature dies and the trigger goes on the stack
        harness.passBothPriorities(); // resolve the return trigger
    }
}
