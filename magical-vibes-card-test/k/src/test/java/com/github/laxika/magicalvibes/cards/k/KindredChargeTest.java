package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KindredCharge.class, GrizzlyBears.class, HillGiant.class, AvianChangeling.class, Clone.class})
class KindredChargeTest extends BaseCardTest {

    @Test
    void copiesEachControlledCreatureOfChosenTypeWithHaste() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        castKindredCharge();
        harness.handleListChoice(player1, "BEAR");

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allMatch(token -> token.hasKeyword(Keyword.HASTE));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getCard().isToken())
                .hasSize(3);
    }

    @Test
    void changelingCountsAsChosenTypeAndTokensExileAtNextEndStep() {
        harness.addToBattlefield(player1, new AvianChangeling());

        castKindredCharge();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> assertThat(token.hasKeyword(Keyword.HASTE)).isTrue());

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void doesNotCopyOpponentsCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castKindredCharge();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .hasSize(1).noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void canChooseATypeWithNoMatchingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castKindredCharge();
        harness.handleListChoice(player1, "DRAGON");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(1).noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesDoNotInheritCountersOrTappedStatus() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castKindredCharge();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.isTapped()).isFalse();
                    assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
                });
        assertThat(original.isTapped()).isTrue();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void copiesExistingTokensOnceWithoutRecursivelyCopyingNewTokens() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castKindredCharge();
        harness.handleListChoice(player1, "BEAR");

        castKindredCharge();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(3);
    }

    @Test
    void allTokensAreExiledByOneDelayedTrigger() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castKindredCharge();
        harness.handleListChoice(player1, "BEAR");

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(KindredCharge.class);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2).noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void copyingATokenDoesNotCopyTheHasteGrantedByKindredCharge() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castKindredCharge();
        harness.handleListChoice(player1, "BEAR");
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getOriginalCard() instanceof Clone)
                .singleElement().satisfies(copy -> {
                    assertThat(copy.hasKeyword(Keyword.HASTE)).isFalse();
                    assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
                });
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private void castKindredCharge() {
        harness.castFromHand(player1, new KindredCharge(), "{4}{R}{R}");
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
