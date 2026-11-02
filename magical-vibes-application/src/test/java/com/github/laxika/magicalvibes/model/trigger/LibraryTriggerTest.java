package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The draw, discard, cycle, scry, surveil, library search and shuffle triggers: {@link LibraryTrigger}. */
class LibraryTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);

    @Test
    void aPlayerActionFunctionsFromTheBattlefield() {
        assertThat(Triggers.youDrawCard().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.opponentDiscardsCard().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.playerCyclesCard().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.youSurveil().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.opponentShufflesLibrary().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void thisCardCycledOrDiscardedFunctionsFromWhereTheCardWindsUp() {
        AbilityZones windsUp = AbilityZones.of(Zone.GRAVEYARD, Zone.EXILE, Zone.LIBRARY);

        assertThat(Triggers.youCycleThisCard().defaultZones()).isEqualTo(windsUp);
        assertThat(Triggers.youDiscardThisCard().defaultZones()).isEqualTo(windsUp);
        assertThat(Triggers.opponentCausesYouToDiscardThisCard().defaultZones()).isEqualTo(windsUp);
        assertThat(windsUp.functionsIn(Zone.HAND)).isFalse();
        assertThat(windsUp.functionsIn(Zone.BATTLEFIELD)).isFalse();
    }

    @Test
    void namesWhatThePlayerDoesAndWhoDoesIt() {
        assertThat(Triggers.youDrawCard())
                .isEqualTo(new LibraryTrigger(LibraryTrigger.LibraryEvent.DRAWS, PlayerRelation.YOU))
                .isNotEqualTo(Triggers.opponentDrawsCard())
                .isNotEqualTo(Triggers.enchantedPlayerDrawsCard())
                .isNotEqualTo(Triggers.youDrawSecondCardEachTurn());
        assertThat(Triggers.youDrawSecondCardEachTurn()).isNotEqualTo(Triggers.opponentDrawsSecondCardEachTurn());
        assertThat(Triggers.youDiscardCard())
                .isEqualTo(new LibraryTrigger(LibraryTrigger.LibraryEvent.DISCARDS, PlayerRelation.YOU))
                .isNotEqualTo(Triggers.opponentDiscardsCard())
                .isNotEqualTo(Triggers.youDiscardOneOrMoreCards());
        assertThat(Triggers.youDiscardThisCard())
                .isEqualTo(new LibraryTrigger(LibraryTrigger.LibraryEvent.THIS_DISCARDED, PlayerRelation.ANY))
                .isNotEqualTo(Triggers.opponentCausesYouToDiscardThisCard());
        assertThat(Triggers.youCycleThisCard()).isNotEqualTo(Triggers.playerCyclesCard());
        assertThat(Triggers.youScry()).isNotEqualTo(Triggers.opponentScries()).isNotEqualTo(Triggers.youSurveil());
        assertThat(Triggers.youSurveil()).isNotEqualTo(Triggers.opponentSurveils());
        assertThat(Triggers.youSearchYourLibrary()).isNotEqualTo(Triggers.opponentSearchesTheirLibrary());
        assertThat(Triggers.playerShufflesLibrary())
                .isNotEqualTo(Triggers.opponentShufflesLibrary())
                .isNotEqualTo(Triggers.spellOrAbilityCausesItsControllerToShuffle());
    }

    @Test
    void needsAnEventAndAPlayer() {
        assertThatThrownBy(() -> new LibraryTrigger(null, PlayerRelation.YOU))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LibraryTrigger(LibraryTrigger.LibraryEvent.DRAWS, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsLibraryAbilitiesByTriggerAndZone() {
        Card card = new Card();
        card.addTrigger(Triggers.youDrawSecondCardEachTurn(), DRAW);
        card.addTrigger(new TriggerCondition(Triggers.youDrawSecondCardEachTurn(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, GAIN);

        assertThat(card.getTriggerEffects(Triggers.youDrawSecondCardEachTurn(), Zone.BATTLEFIELD))
                .containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.youDrawSecondCardEachTurn(), Zone.GRAVEYARD))
                .containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.youDrawCard(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void aCycledCardReadsItsCyclingAbilityFromTheGraveyard() {
        Card card = new Card();
        card.addTrigger(Triggers.youCycleThisCard(), DRAW);

        assertThat(card.getTriggerEffects(Triggers.youCycleThisCard(), Zone.GRAVEYARD)).containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.youCycleThisCard(), Zone.BATTLEFIELD)).isEmpty();
    }
}
