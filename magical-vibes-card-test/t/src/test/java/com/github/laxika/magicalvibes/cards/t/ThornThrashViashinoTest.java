package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CourtArchers;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfJund;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThornThrashViashino.class, CourtArchers.class, ObeliskOfJund.class})
class ThornThrashViashinoTest extends BaseCardTest {

    private void castViashino() {
        harness.castFromHand(player1, new ThornThrashViashino(), "{3}{R}");
        harness.passBothPriorities();
    }

    @Test
    void devourTwoAddsFourCountersAndSacrificesOnlyChosenCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CourtArchers());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CourtArchers());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CourtArchers());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new CourtArchers());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ObeliskOfJund());
        castViashino();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        Permanent viashino = findPermanent(player1, "Thorn-Thrash Viashino");
        assertThat(viashino.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivor, artifact, viashino)
                .doesNotContain(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponent);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
    }

    @Test
    void mayDeclineDevourWhenCreaturesAreAvailable() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CourtArchers());
        castViashino();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(findPermanent(player1, "Thorn-Thrash Viashino")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noOwnCreaturesEntersWithoutDevourPrompt() {
        harness.addToBattlefield(player2, new CourtArchers());
        harness.addToBattlefield(player1, new ObeliskOfJund());
        castViashino();
        assertThat(findPermanent(player1, "Thorn-Thrash Viashino")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void greenManaGrantsOnlySelfTrampleUntilEndOfTurnWithoutTapping() {
        castViashino();
        Permanent viashino = findPermanent(player1, "Thorn-Thrash Viashino");
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new CourtArchers());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.hasKeyword(gd, viashino, Keyword.TRAMPLE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, viashino, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isFalse();
        assertThat(viashino.isTapped()).isFalse();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, viashino, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void trampleAbilityCanBeActivatedRepeatedlyWhileTapped() {
        Permanent viashino = harness.addToBattlefieldAndReturn(player1, new ThornThrashViashino());
        viashino.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, viashino, Keyword.TRAMPLE)).isTrue();
        assertThat(viashino.isTapped()).isTrue();
    }

    @Test
    void colorlessManaCannotPayTrampleActivation() {
        Permanent viashino = harness.addToBattlefieldAndReturn(player1, new ThornThrashViashino());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.hasKeyword(gd, viashino, Keyword.TRAMPLE)).isFalse();
    }
}
