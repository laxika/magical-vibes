package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TemporaryInsanity.class, Forest.class, GrizzlyBears.class})
class TemporaryInsanityTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control, untaps, and grants haste to a creature below the graveyard count")
    void resolvesControlUntapAndHaste() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        cast(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature whose power equals the graveyard count")
    void requiresStrictlyLessPower() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemporaryInsanity()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts only the caster's graveyard")
    void countsOnlyCastersGraveyard() {
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemporaryInsanity()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Becomes illegal if the caster's graveyard shrinks before resolution")
    void fizzlesWhenGraveyardShrinksBeforeResolution() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemporaryInsanity()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new TemporaryInsanity()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
