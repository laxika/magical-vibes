package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TitanicUltimatumTest extends BaseCardTest {

    @Test
    @DisplayName("Gives own creatures +5/+5 and first strike, trample, lifelink; opponent's untouched")
    void buffsOwnCreatures() {
        Permanent own = addReadyCreature(player1, new GrizzlyBears());
        Permanent enemy = addReadyCreature(player2, new GrizzlyBears());
        castUltimatum();

        assertThat(own.getEffectivePower()).isEqualTo(7);
        assertThat(own.getEffectiveToughness()).isEqualTo(7);
        assertThat(own.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(own.hasKeyword(Keyword.LIFELINK)).isTrue();

        assertThat(enemy.getEffectivePower()).isEqualTo(2);
        assertThat(enemy.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(enemy.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(enemy.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOff() {
        Permanent own = addReadyCreature(player1, new GrizzlyBears());
        castUltimatum();

        assertThat(own.getEffectivePower()).isEqualTo(7);
        assertThat(own.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(own.getEffectiveToughness()).isEqualTo(2);
        assertThat(own.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
        assertThat(own.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(own.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    private void castUltimatum() {
        harness.setHand(player1, List.of(new TitanicUltimatum()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
