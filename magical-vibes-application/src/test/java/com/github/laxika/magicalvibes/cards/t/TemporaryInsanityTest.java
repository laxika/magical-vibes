package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AerieWorshippers;
import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({TemporaryInsanity.class, Forest.class, DragonWhelp.class})
class TemporaryInsanityTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control, untaps, and grants haste to a creature below the graveyard count")
    void resolvesControlUntapAndHaste() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent target = addCreatureReady(player2, new DragonWhelp());
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
        Permanent target = addCreatureReady(player2, new DragonWhelp());
        harness.setHand(player1, List.of(new TemporaryInsanity()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts only the caster's graveyard")
    void countsOnlyCastersGraveyard() {
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent target = addCreatureReady(player2, new DragonWhelp());
        harness.setHand(player1, List.of(new TemporaryInsanity()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Becomes illegal if the caster's graveyard shrinks before resolution")
    void fizzlesWhenGraveyardShrinksBeforeResolution() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent target = addCreatureReady(player2, new DragonWhelp());
        harness.setHand(player1, List.of(new TemporaryInsanity()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void requiresCreatureTarget() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TemporaryInsanity()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent target = addCreatureReady(player2, new DragonWhelp());

        cast(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @CardUsed(AerieWorshippers.class)
    @DisplayName("Untaps before gaining control so the target's untap trigger keeps its controller")
    void untapsBeforeGainingControl() {
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent target = addCreatureReady(player2, new AerieWorshippers());
        target.tap();
        harness.setHand(player1, List.of(new TemporaryInsanity()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(target.getCard().getId())
                && player2.getId().equals(entry.getControllerId()));
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new TemporaryInsanity()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
