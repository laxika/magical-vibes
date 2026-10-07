package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KyoshiWarriors;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SukiCourageousRescuer.class, GrizzlyBears.class, ZuranOrb.class, Forest.class,
        DayOfJudgment.class, KyoshiWarriors.class})
class SukiCourageousRescuerTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts other creatures and creates an Ally when a permanent leaves during your turn")
    void boostsOtherCreaturesAndCreatesAlly() {
        Permanent suki = harness.addToBattlefieldAndReturn(player1, new SukiCourageousRescuer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, suki)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 2, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Creates only one Ally each turn and not during an opponent's turn")
    void createsOnlyOnceDuringOwnTurn() {
        harness.addToBattlefield(player1, new SukiCourageousRescuer());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Triggers when Suki and another creature leave simultaneously")
    void triggersWhenDestroyedAlongsideAnotherCreature() {
        harness.addToBattlefield(player1, new SukiCourageousRescuer());
        harness.addToBattlefield(player1, new KyoshiWarriors());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new DayOfJudgment(), "{2}{W}{W}");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Suki, Courageous Rescuer");
        harness.assertInGraveyard(player1, "Kyoshi Warriors");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("Returning another permanent to hand triggers and boosts the created Ally")
    void returningPermanentCreatesBoostedAlly() {
        harness.addToBattlefield(player1, new SukiCourageousRescuer());
        Permanent warriors = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriors());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KyoshiWarriors());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(3);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, warriors));
        resolveAllTriggers();

        harness.assertInHand(player1, "Kyoshi Warriors");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Exiling another permanent triggers only once even before the trigger resolves")
    void exilingPermanentsBeforeResolutionTriggersOnce() {
        harness.addToBattlefield(player1, new SukiCourageousRescuer());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriors());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriors());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToExile(gd, first);
            harness.getPermanentRemovalService().removePermanentToExile(gd, second);
        });
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("A token leaving the battlefield also creates an Ally")
    void tokenDepartureCreatesAlly() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new KyoshiWarriors(), "{3}{W}");
        resolveAllTriggers();
        Permanent originalToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        harness.addToBattlefield(player1, new SukiCourageousRescuer());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, originalToken));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> assertThat(token.getId()).isNotEqualTo(originalToken.getId()));
    }

    @Test
    @DisplayName("An opponent's permanent leaving and Suki herself leaving do not trigger")
    void ignoresOpponentPermanentAndOwnDeparture() {
        Permanent suki = harness.addToBattlefieldAndReturn(player1, new SukiCourageousRescuer());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new KyoshiWarriors());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, opponent));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, suki));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A departure during an opponent's turn does not consume the next own-turn trigger")
    void opponentTurnDepartureDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new SukiCourageousRescuer());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriors());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriors());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, first));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        harness.forceActivePlayer(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, second));
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }
}
