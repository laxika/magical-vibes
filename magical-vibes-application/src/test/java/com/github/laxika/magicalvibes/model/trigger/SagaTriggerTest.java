package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.SagaChapterTargets;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The Saga triggers: {@link ChapterTrigger} and {@link FinalChapterTrigger}. */
class SagaTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);

    @Test
    void everySagaTriggerFunctionsFromTheBattlefieldByDefault() {
        assertThat(List.of(Triggers.chapter(1), Triggers.chapter(6), Triggers.finalChapterAbilityTriggers(),
                Triggers.finalChapterAbilityResolves())).allSatisfy(trigger ->
                assertThat(trigger.defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD));
    }

    @Test
    void aChapterTriggerNamesItsChapterAndNumeral() {
        assertThat(Triggers.chapter(3)).isEqualTo(new ChapterTrigger(3)).isNotEqualTo(Triggers.chapter(2));
        assertThat(List.of(1, 2, 3, 4, 5, 6)).extracting(chapter -> Triggers.chapter(chapter).numeral())
                .containsExactly("I", "II", "III", "IV", "V", "VI");
    }

    @Test
    void aChapterNumberIsAtLeastOne() {
        assertThatThrownBy(() -> Triggers.chapter(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theFinalChapterTriggersNameTheirEvent() {
        assertThat(Triggers.finalChapterAbilityTriggers())
                .isEqualTo(new FinalChapterTrigger(FinalChapterTrigger.FinalChapterEvent.TRIGGERS))
                .isNotEqualTo(Triggers.finalChapterAbilityResolves());
        assertThat(Triggers.finalChapterAbilityResolves())
                .isEqualTo(new FinalChapterTrigger(FinalChapterTrigger.FinalChapterEvent.RESOLVES));
        assertThatThrownBy(() -> new FinalChapterTrigger(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void theEffectsOfOneChapterAreOneAbility() {
        Card card = new Card();
        card.addTrigger(Triggers.chapter(1), DRAW);
        card.addTrigger(Triggers.chapter(2), GAIN);
        card.addTrigger(Triggers.chapter(1), GAIN);

        assertThat(card.getTriggeredAbilities()).hasSize(2);
        TriggeredAbility chapterOne = card.getTriggeredAbilities().getFirst();
        assertThat(chapterOne.hasTrigger(Triggers.chapter(1))).isTrue();
        assertThat(chapterOne.effects()).extracting(EffectRegistration::effect).containsExactly(DRAW, GAIN);
        assertThat(card.getTriggerEffects(Triggers.chapter(1), Zone.BATTLEFIELD)).containsExactly(DRAW, GAIN);
        assertThat(card.getTriggerEffects(Triggers.chapter(2))).containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.chapter(3))).isEmpty();
    }

    @Test
    void simultaneousEffectsJoinTheirChapterAbility() {
        Card card = new Card();
        card.addTrigger(Triggers.chapter(1), DRAW);
        card.addSimultaneousTrigger(Triggers.chapter(1), GAIN, DRAW);

        assertThat(card.getTriggeredAbilities()).singleElement().satisfies(ability ->
                assertThat(ability.effects()).extracting(EffectRegistration::simultaneousWithPrevious)
                        .containsExactly(false, false, true));
    }

    @Test
    void aChapterAddedAgainEffectByEffectKeepsItsTargets() {
        SagaChapterTargets targets = SagaChapterTargets.NONE.withFilters(Set.of(TargetFilters.creature()));
        TriggeredAbility tap = new TriggeredAbility(Triggers.chapter(1), DRAW).withChapterTargets(targets);
        TriggeredAbility gain = new TriggeredAbility(Triggers.chapter(1), GAIN).withChapterTargets(targets);
        Card card = new Card();
        card.addTriggeredAbility(tap);
        card.addTriggeredAbility(gain);

        assertThat(card.getTriggeredAbilities()).singleElement().satisfies(ability -> {
            assertThat(ability.effects()).extracting(EffectRegistration::effect).containsExactly(DRAW, GAIN);
            assertThat(ability.chapterTargets()).isEqualTo(targets);
        });
    }

    @Test
    void aChapterWithoutTargetsTakesTheTargetsOfAnAbilityAddedToIt() {
        SagaChapterTargets targets = SagaChapterTargets.NONE.withFilters(Set.of(TargetFilters.creature()));
        Card card = new Card();
        card.addTrigger(Triggers.chapter(1), DRAW);
        card.addTriggeredAbility(new TriggeredAbility(Triggers.chapter(1), GAIN).withChapterTargets(targets));

        assertThat(card.getSagaChapterTargetFilters(1)).containsExactly(TargetFilters.creature());
    }

    @Test
    void theFinalChapterIsTheGreatestChapter() {
        Card card = new Card();
        assertThat(card.getSagaFinalChapter()).isZero();

        card.addTrigger(Triggers.chapter(1), DRAW);
        card.addTrigger(Triggers.chapter(3), DRAW);
        card.addTrigger(Triggers.chapter(2), DRAW);
        card.addTrigger(Triggers.finalChapterAbilityResolves(), GAIN);

        assertThat(card.getSagaFinalChapter()).isEqualTo(3);
    }
}
