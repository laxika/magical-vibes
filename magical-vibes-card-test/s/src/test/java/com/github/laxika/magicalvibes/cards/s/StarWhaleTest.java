package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarWhale.class, GrizzlyBears.class, Shock.class, RodOfRuin.class})
class StarWhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have ward {2}")
    void grantsWardToOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new StarWhale());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Paying ward {2} lets a spell targeting another creature resolve")
    void payingWardLetsSpellResolve() {
        harness.addToBattlefield(player1, new StarWhale());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Star Whale does not grant ward to itself")
    void doesNotGrantWardToItself() {
        Permanent whale = harness.addToBattlefieldAndReturn(player1, new StarWhale());
        prepareOpponentMainPhase();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, whale.getId());
        harness.passBothPriorities();

        assertThat(whale.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Suspend exiles Star Whale with six time counters")
    void suspendExilesWithSixTimeCounters() {
        StarWhale whale = new StarWhale();
        harness.setHand(player1, List.of(whale));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(whale);
        assertThat(gd.exiledCardTimeCounters).containsEntry(whale.getId(), 6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward also counters an opponent's targeted activated ability unless they pay")
    void wardCountersOpponentsActivatedAbility() {
        harness.addToBattlefield(player1, new StarWhale());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addCreatureReady(player2, new RodOfRuin());
        prepareOpponentMainPhase();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, 0, null, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's spells")
    void ownSpellDoesNotTriggerWard() {
        harness.addToBattlefield(player1, new StarWhale());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Star Whale does not grant ward to opposing creatures")
    void opposingCreaturesDoNotGainWard() {
        harness.addToBattlefield(player1, new StarWhale());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Time counters are removed on the owner's upkeep, not the opponent's")
    void suspendCountersFollowOwnersUpkeep() {
        StarWhale whale = suspendWhale();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(whale.getId(), 6);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(whale.getId(), 5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(whale);
    }

    @Test
    @DisplayName("The last counter allows a free cast and Star Whale can attack immediately")
    void suspendCastsForFreeAndGrantsHaste() {
        StarWhale whale = suspendWhale();
        for (int i = 0; i < 6; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Star Whale");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(whale);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(whale.getId());
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(findPermanent(player1, "Star Whale").isTapped()).isFalse();
        resolveCombat(player1);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Declining the free cast leaves Star Whale exiled without time counters")
    void mayDeclineSuspendCast() {
        StarWhale whale = suspendWhale();
        for (int i = 0; i < 6; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        harness.handleMayAbilityChosen(player1, false);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(whale);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(whale.getId());
        harness.assertNotOnBattlefield(player1, "Star Whale");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Star Whale cannot be suspended during upkeep without flash permission")
    void cannotSuspendDuringUpkeep() {
        StarWhale whale = new StarWhale();
        harness.setHand(player1, List.of(whale));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Star Whale");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(whale);
    }

    private StarWhale suspendWhale() {
        StarWhale whale = new StarWhale();
        harness.setHand(player1, List.of(whale));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return whale;
    }

    private void prepareOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
