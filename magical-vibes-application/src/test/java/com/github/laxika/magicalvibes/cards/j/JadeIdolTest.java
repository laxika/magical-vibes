package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({JadeIdol.class, CallousDeceiver.class, ReachThroughMists.class, HumbleBudoka.class})
class JadeIdolTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell animates the idol as a 4/4 Spirit artifact creature")
    void spiritSpellAnimatesIdol() {
        Permanent idol = addIdol();
        prepareMainPhase();
        harness.setHand(player1, List.of(new CallousDeceiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(idol.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, idol)).isTrue();
        assertThat(gqs.isArtifact(idol)).isTrue();
        assertThat(gqs.getEffectivePower(gd, idol)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, idol)).isEqualTo(4);
        assertThat(idol.getTransientSubtypes()).contains(CardSubtype.SPIRIT);
    }

    @Test
    @DisplayName("Casting an Arcane spell animates the idol")
    void arcaneSpellAnimatesIdol() {
        Permanent idol = addIdol();
        prepareMainPhase();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(idol.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, idol)).isTrue();
    }

    @Test
    @DisplayName("A matching spell cast by an opponent does not animate the idol")
    void opponentSpellDoesNotAnimateIdol() {
        Permanent idol = addIdol();
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new ReachThroughMists()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(idol.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, idol)).isFalse();
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not animate the idol")
    void unrelatedSpellDoesNotAnimateIdol() {
        Permanent idol = addIdol();
        prepareMainPhase();
        harness.setHand(player1, List.of(new HumbleBudoka()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(idol.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, idol)).isFalse();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        Permanent idol = addIdol();
        prepareMainPhase();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(idol.isAnimatedUntilEndOfTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(idol.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, idol)).isFalse();
    }

    private Permanent addIdol() {
        return harness.addToBattlefieldAndReturn(player1, new JadeIdol());
    }

    private void prepareMainPhase() {
        prepareMainPhase(player1);
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
