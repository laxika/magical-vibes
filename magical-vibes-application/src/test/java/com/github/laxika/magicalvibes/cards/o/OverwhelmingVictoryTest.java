package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({OverwhelmingVictory.class, GrizzlyBears.class, HillGiant.class})
class OverwhelmingVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("deals 5 damage and boosts own creatures by the excess")
    void boostsByExcessDamageAndGrantsTrample() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());

        castOverwhelmingVictory(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(5);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(3);
        assertThat(opponentCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("gives no power boost when no excess damage is dealt")
    void givesNoBoostWithoutExcessDamage() {
        HillGiant targetCard = new HillGiant();
        targetCard.setToughness(5);
        Permanent target = addCreatureReady(player2, targetCard);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        castOverwhelmingVictory(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        castOverwhelmingVictory(target);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(5);
        assertThat(ownCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new OverwhelmingVictory()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castOverwhelmingVictory(Permanent target) {
        harness.setHand(player1, List.of(new OverwhelmingVictory()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
