package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindSail.class, AlabornTrooper.class, Mountain.class})
class WindSailTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Both target creatures gain flying")
    void twoTargetsGainFlying() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        harness.setHand(player1, List.of(new WindSail()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(a.getId(), b.getId()));

        assertThat(gqs.hasKeyword(gd, a, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, b, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("May target only one creature")
    void singleTargetAllowed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        harness.setHand(player1, List.of(new WindSail()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(bears.getId()));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void wearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        harness.setHand(player1, List.of(new WindSail()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(bears.getId()));
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Still affects a remaining legal target if another target leaves before resolution")
    void resolvesForRemainingLegalTarget() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        harness.setHand(player1, List.of(new WindSail()));
        giveMana();

        harness.castSorcery(player1, 0, List.of(removed.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, remaining, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target the same creature twice")
    void cannotTargetSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlabornTrooper());
        harness.setHand(player1, List.of(new WindSail()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new WindSail()));
        giveMana();

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
