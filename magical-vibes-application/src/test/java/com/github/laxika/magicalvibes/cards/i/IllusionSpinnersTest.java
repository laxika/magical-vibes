package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.s.SpellstutterSprite;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IllusionSpinnersTest extends BaseCardTest {

    @Test
    @DisplayName("Illusion Spinners has hexproof while untapped")
    void hasHexproofWhileUntapped() {
        Permanent spinners = addCreatureReady(player1, new IllusionSpinners());

        assertThat(gqs.hasKeyword(gd, spinners, Keyword.HEXPROOF)).isTrue();

        spinners.tap();

        assertThat(gqs.hasKeyword(gd, spinners, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("An opponent cannot target untapped Illusion Spinners")
    void opponentCannotTargetUntappedSpinners() {
        Permanent spinners = addCreatureReady(player1, new IllusionSpinners());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, spinners.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("An opponent can target Illusion Spinners after it becomes tapped")
    void opponentCanTargetTappedSpinners() {
        Permanent spinners = addCreatureReady(player1, new IllusionSpinners());
        spinners.tap();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player2, 0, 0, spinners.getId(), null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Controlling a Faerie allows Illusion Spinners to be cast at instant speed")
    void faerieGrantsFlashTiming() {
        harness.addToBattlefield(player1, new SpellstutterSprite());
        prepareInstantSpeedCast();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Without a Faerie, Illusion Spinners keeps sorcery timing")
    void noFaerieKeepsSorceryTiming() {
        prepareInstantSpeedCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void prepareInstantSpeedCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new IllusionSpinners()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
