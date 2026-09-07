package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BenalishHero;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Seasinger;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimeElemental.class, BenalishHero.class, GrizzlyBears.class, HolyStrength.class,
        Island.class, Seasinger.class, Unsummon.class})
class TimeElementalTest extends BaseCardTest {

    // ===== Attack / block delayed sacrifice + self damage =====

    @Test
    @DisplayName("Attacking sacrifices Time Elemental and deals 5 damage to its controller at end of combat")
    void attackingSacrificesAndDealsFiveToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new TimeElemental());
        declareAttackers(player1, List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Time Elemental");
        harness.assertInGraveyard(player1, "Time Elemental");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Blocking sacrifices Time Elemental and deals 5 damage to its controller at end of combat")
    void blockingSacrificesAndDealsFiveToController() {
        harness.setLife(player2, 20);

        addCreatureReady(player2, new TimeElemental());

        // 1/1 attacker so Time Elemental (0/2) survives combat damage and reaches the end-of-combat sacrifice.
        Permanent attacker = addCreatureReady(player1, new BenalishHero());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player2, "Time Elemental");
        harness.assertInGraveyard(player2, "Time Elemental");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    // ===== Activated bounce ability =====

    @Test
    @DisplayName("Activated ability returns a target permanent that isn't enchanted to its owner's hand")
    void activatedAbilityBouncesTargetPermanent() {
        Permanent elemental = addCreatureReady(player1, new TimeElemental());
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(elemental.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability cannot target an enchanted permanent")
    void activatedAbilityCannotTargetEnchantedPermanent() {
        addCreatureReady(player1, new TimeElemental());
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(bears.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability returns an unenchanted land to its owner's hand")
    void activatedAbilityBouncesUnenchantedLand() {
        addCreatureReady(player1, new TimeElemental());
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.activateAbility(player1, 0, null, island.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(island);
        assertThat(gd.playerHands.get(player2.getId())).contains(island.getCard());
    }

    @Test
    @DisplayName("Activated ability does not return a target that becomes enchanted before resolution")
    void activatedAbilityDoesNotBounceTargetThatBecomesEnchantedBeforeResolution() {
        addCreatureReady(player1, new TimeElemental());
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, bears.getId());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(bears.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(bears.getCard());
    }

    @Test
    @DisplayName("Attacking still deals 5 damage if Time Elemental leaves before its trigger resolves")
    void attackingStillDealsDamageIfElementalLeavesBeforeTriggerResolves() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent elemental = addCreatureReady(player1, new TimeElemental());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elemental.getId());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerHands.get(player1.getId())).contains(elemental.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Attacking cannot sacrifice a Time Elemental no longer controlled at end of combat")
    void attackingCannotSacrificeElementalNoLongerControlled() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent elemental = addCreatureReady(player1, new TimeElemental());
        harness.addToBattlefield(player1, new Island());
        addCreatureReady(player2, new Seasinger());
        harness.addToBattlefield(player2, new Island());

        declareAttackers(player1, List.of(0));
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, elemental.getId());
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elemental);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(elemental.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(elemental.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }
}
