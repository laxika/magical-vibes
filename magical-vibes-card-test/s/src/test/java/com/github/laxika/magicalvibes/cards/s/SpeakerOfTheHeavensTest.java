package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpeakerOfTheHeavens.class})
class SpeakerOfTheHeavensTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 4/4 white Angel token with flying at the life threshold")
    void createsAngelTokenAtLifeThreshold() {
        addCreatureReady(player1, new SpeakerOfTheHeavens());
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
        Permanent speaker = addCreatureReady(player1, new SpeakerOfTheHeavens());
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL + 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("starting life total");
        assertThat(speaker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate outside sorcery timing")
    void cannotActivateOutsideSorceryTiming() {
        addCreatureReady(player1, new SpeakerOfTheHeavens());
        harness.setLife(player1, GameData.STARTING_LIFE_TOTAL + 7);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotActivateBelowCommanderLifeThreshold() {
        gd.format = DeckFormat.COMMANDER;
        Permanent speaker = addCreatureReady(player1, new SpeakerOfTheHeavens());
        harness.setLife(player1, gd.startingLife() + 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("starting life total");
        assertThat(speaker.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createsAngelAtCommanderLifeThreshold() {
        gd.format = DeckFormat.COMMANDER;
        addCreatureReady(player1, new SpeakerOfTheHeavens());
        harness.setLife(player1, gd.startingLife() + 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
    }

    @Test
    void lifeLossAfterActivationDoesNotPreventTokenCreation() {
        Permanent speaker = addCreatureReady(player1, new SpeakerOfTheHeavens());
        harness.setLife(player1, gd.startingLife() + 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        assertThat(speaker.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Angel")).isZero();
        harness.setLife(player1, gd.startingLife());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Angel")).isEqualTo(1);
    }

    @Test
    void cannotActivateDuringUpkeep() {
        Permanent speaker = addCreatureReady(player1, new SpeakerOfTheHeavens());
        harness.setLife(player1, gd.startingLife() + 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(speaker.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithAnotherAbilityOnStack() {
        addCreatureReady(player1, new SpeakerOfTheHeavens());
        Permanent second = addCreatureReady(player1, new SpeakerOfTheHeavens());
        harness.setLife(player1, gd.startingLife() + 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new SpeakerOfTheHeavens());
        harness.setLife(player1, gd.startingLife() + 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent speaker = addCreatureReady(player1, new SpeakerOfTheHeavens());
        speaker.tap();
        harness.setLife(player1, gd.startingLife() + 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void attacksWithoutTappingAndGainsLifeFromCombatDamage() {
        Permanent speaker = addCreatureReady(player1, new SpeakerOfTheHeavens());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(speaker.isTapped()).isFalse();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
