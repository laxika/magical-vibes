package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpeakerOfTheHeavensTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 white Angel token with flying at the life threshold")
    void createsAngelTokenAtLifeThreshold() {
        addReadySpeaker(player1);
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL + 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(angel.getCard().getPower()).isEqualTo(4);
        assertThat(angel.getCard().getToughness()).isEqualTo(4);
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate below seven life above starting life")
    void cannotActivateBelowLifeThreshold() {
        Permanent speaker = addReadySpeaker(player1);
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL + 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("starting life total");
        assertThat(speaker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate outside sorcery timing")
    void cannotActivateOutsideSorceryTiming() {
        addReadySpeaker(player1);
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL + 7);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadySpeaker(Player player) {
        Permanent perm = new Permanent(new SpeakerOfTheHeavens());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
