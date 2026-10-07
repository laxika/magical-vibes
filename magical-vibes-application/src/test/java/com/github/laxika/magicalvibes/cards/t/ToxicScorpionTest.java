package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
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

@CardUsed({ToxicScorpion.class, DawnhartDisciple.class, Abrade.class})
class ToxicScorpionTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another creature you control deathtouch")
    void etbGrantsDeathtouchToAnotherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        castScorpion(bears);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("ETB deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        castScorpion(bears);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new DawnhartDisciple());
        harness.setHand(player1, List.of(new ToxicScorpion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    @DisplayName("Can be cast without a target when no other creature is controlled")
    void canBeCastWithoutTarget() {
        harness.setHand(player1, List.of(new ToxicScorpion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB still grants deathtouch after Toxic Scorpion leaves the battlefield")
    void triggerResolvesAfterSourceDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        castScorpion(target);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 0, harness.getPermanentId(player1, "Toxic Scorpion"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        assertThat(target.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not grant deathtouch to a replacement creature after its target dies")
    void triggerDoesNotAffectReplacementForDeadTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        castScorpion(target);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target).contains(replacement);
        assertThat(replacement.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another Toxic Scorpion is a legal target and only the chosen creature gains deathtouch")
    void canTargetAnotherScorpionWithoutGrantingToOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ToxicScorpion());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DawnhartDisciple());
        castScorpion(target);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
        assertThat(gd.stack).isEmpty();
    }

    private void castScorpion(Permanent target) {
        harness.setHand(player1, List.of(new ToxicScorpion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, target.getId());
    }
}
