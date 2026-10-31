package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.LegacySlotTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * How {@link Card} turns slot registrations into abilities, and how the slot views read them back.
 */
class CardAbilitiesTest {

    private static final CardEffect DRAW_ONE = new DrawCardEffect(1);
    private static final CardEffect DRAW_TWO = new DrawCardEffect(2);
    private static final CardEffect GAIN_ONE = new GainLifeEffect(1);
    private static final CardEffect GAIN_TWO = new GainLifeEffect(2);

    private final Card card = new Card();

    @Test
    void spellEffectsFormOneSpellAbility() {
        card.addEffect(EffectSlot.SPELL, DRAW_ONE);
        card.addSimultaneousEffects(EffectSlot.SPELL, GAIN_ONE, GAIN_TWO);

        assertThat(card.getAbilities()).containsExactly(new SpellAbility(List.of(
                new EffectRegistration(DRAW_ONE),
                new EffectRegistration(GAIN_ONE),
                new EffectRegistration(GAIN_TWO, TriggerMode.NORMAL, true))));
        assertThat(card.getEffects(EffectSlot.SPELL)).containsExactly(DRAW_ONE, GAIN_ONE, GAIN_TWO);
    }

    @Test
    void eachStaticEffectIsAStaticAbilityOfItsZone() {
        card.addEffect(EffectSlot.STATIC, DRAW_ONE);
        card.addEffect(EffectSlot.STATIC, DRAW_TWO);
        card.addEffect(EffectSlot.COMMAND_ZONE_STATIC, GAIN_ONE);

        assertThat(card.getAbilities()).containsExactly(
                new StaticAbility(List.of(DRAW_ONE)),
                new StaticAbility(List.of(DRAW_TWO)),
                new StaticAbility(List.of(GAIN_ONE), AbilityZones.COMMAND));
        assertThat(card.getEffects(EffectSlot.STATIC)).containsExactly(DRAW_ONE, DRAW_TWO);
        assertThat(card.getEffectRegistrations(EffectSlot.COMMAND_ZONE_STATIC))
                .containsExactly(new EffectRegistration(GAIN_ONE));
    }

    @Test
    void aStaticEffectCanNeitherTriggerNorBeSimultaneous() {
        assertThatThrownBy(() -> card.addEffect(EffectSlot.STATIC, DRAW_ONE, TriggerMode.INDEPENDENT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> card.addSimultaneousEffects(EffectSlot.STATIC, DRAW_ONE, DRAW_TWO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(card.getAbilities()).isEmpty();
    }

    @Test
    void aSlotGroupedEachGetsOneAbilityPerRegistration() {
        card.addEffect(EffectSlot.ON_DEATH, DRAW_ONE);
        card.addEffect(EffectSlot.ON_DEATH, DRAW_TWO);

        assertThat(card.getAbilities()).containsExactly(
                legacy(EffectSlot.ON_DEATH, new EffectRegistration(DRAW_ONE)),
                legacy(EffectSlot.ON_DEATH, new EffectRegistration(DRAW_TWO)));
        assertThat(card.getEffects(EffectSlot.ON_DEATH)).containsExactly(DRAW_ONE, DRAW_TWO);
    }

    @Test
    void simultaneousEffectsStayInOneAbilityInASlotGroupedEach() {
        card.addEffect(EffectSlot.ON_DEATH, DRAW_ONE);
        card.addSimultaneousEffects(EffectSlot.ON_DEATH, GAIN_ONE, GAIN_TWO);

        assertThat(card.getAbilities()).containsExactly(
                legacy(EffectSlot.ON_DEATH, new EffectRegistration(DRAW_ONE)),
                legacy(EffectSlot.ON_DEATH, new EffectRegistration(GAIN_ONE),
                        new EffectRegistration(GAIN_TWO, TriggerMode.NORMAL, true)));
        assertThat(card.isSimultaneousWithPrevious(GAIN_TWO)).isTrue();
        assertThat(card.isSimultaneousWithPrevious(GAIN_ONE)).isFalse();
    }

    @Test
    void aSlotGroupedAllGetsOneAbilityAtThePositionOfItsFirstRegistration() {
        card.addEffect(EffectSlot.ON_BLOCK, DRAW_ONE);
        card.addEffect(EffectSlot.ON_DEATH, GAIN_ONE);
        card.addEffect(EffectSlot.ON_BLOCK, DRAW_TWO, TriggerMode.ONCE_PER_BLOCK);

        assertThat(card.getAbilities()).containsExactly(
                legacy(EffectSlot.ON_BLOCK, new EffectRegistration(DRAW_ONE),
                        new EffectRegistration(DRAW_TWO, TriggerMode.ONCE_PER_BLOCK)),
                legacy(EffectSlot.ON_DEATH, new EffectRegistration(GAIN_ONE)));
        assertThat(card.getEffectRegistrations(EffectSlot.ON_BLOCK)).containsExactly(
                new EffectRegistration(DRAW_ONE), new EffectRegistration(DRAW_TWO, TriggerMode.ONCE_PER_BLOCK));
    }

    @Test
    void independentRegistrationsLeaveTheSharedAbilityOfTheirSlot() {
        card.addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, DRAW_ONE);
        card.addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, GAIN_ONE, TriggerMode.INDEPENDENT);
        card.addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, GAIN_TWO, TriggerMode.INDEPENDENT);
        card.addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, DRAW_TWO);

        assertThat(card.getAbilities()).containsExactly(
                legacy(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new EffectRegistration(DRAW_ONE), new EffectRegistration(DRAW_TWO)),
                legacy(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new EffectRegistration(GAIN_ONE, TriggerMode.INDEPENDENT)),
                legacy(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new EffectRegistration(GAIN_TWO, TriggerMode.INDEPENDENT)));
    }

    @Test
    void slotViewsCantBeChangedFromOutside() {
        card.addEffect(EffectSlot.ON_DEATH, DRAW_ONE);

        assertThatThrownBy(() -> card.getEffectRegistrations(EffectSlot.ON_DEATH).clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> card.getAbilities().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).isEmpty();
        assertThat(card.getEffectRegistrations(EffectSlot.ON_ATTACK)).isEmpty();
    }

    @Test
    void viewsFollowEffectsAddedAfterTheyWereRead() {
        card.addEffect(EffectSlot.ON_DEATH, DRAW_ONE);
        assertThat(card.getEffects(EffectSlot.ON_DEATH)).containsExactly(DRAW_ONE);

        card.addEffect(EffectSlot.ON_DEATH, DRAW_TWO);

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).containsExactly(DRAW_ONE, DRAW_TWO);
    }

    @Test
    void removingEffectsDropsAbilitiesLeftEmptyAndKeepsTheOthersAsTheyAre() {
        card.addEffect(EffectSlot.ON_DEATH, DRAW_ONE);
        card.addEffect(EffectSlot.ON_DEATH, GAIN_ONE);
        Ability untouched = card.getAbilities().get(1);

        card.removeEffectsIf(DRAW_ONE::equals);

        assertThat(card.getAbilities()).hasSize(1);
        assertThat(card.getAbilities().getFirst()).isSameAs(untouched);
        assertThat(card.getEffects(EffectSlot.ON_DEATH)).containsExactly(GAIN_ONE);
    }

    @Test
    void anEffectLeftFirstInItsAbilityIsNoLongerSimultaneous() {
        card.addSimultaneousEffects(EffectSlot.SPELL, DRAW_ONE, GAIN_ONE, GAIN_TWO);

        card.removeEffectsIf(DRAW_ONE::equals);

        assertThat(card.getEffectRegistrations(EffectSlot.SPELL)).containsExactly(
                new EffectRegistration(GAIN_ONE), new EffectRegistration(GAIN_TWO, TriggerMode.NORMAL, true));
    }

    @Test
    void effectsCanBeRemovedFromSomeSlotsOnly() {
        card.addEffect(EffectSlot.STATIC, DRAW_ONE);
        card.addEffect(EffectSlot.ON_DEATH, DRAW_ONE);

        card.removeSlotEffectsIf((slot, effect) -> slot != EffectSlot.STATIC);

        assertThat(card.getEffects(EffectSlot.STATIC)).containsExactly(DRAW_ONE);
        assertThat(card.getEffects(EffectSlot.ON_DEATH)).isEmpty();
    }

    @Test
    void sagaChapterTargetsAreHeldByTheChapterAbility() {
        SagaChapterTargetGroup group = new SagaChapterTargetGroup(TargetFilters.creatureAnOpponentControls(), 0, 1);
        card.addEffect(EffectSlot.SAGA_CHAPTER_I, DRAW_ONE);
        card.setSagaChapterTargetFilter(EffectSlot.SAGA_CHAPTER_I, Set.of(TargetFilters.creatureYouControl()));
        card.setSagaChapterTargetRequired(EffectSlot.SAGA_CHAPTER_I);
        card.setSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I, List.of(group));
        card.addEffect(EffectSlot.SAGA_CHAPTER_I, GAIN_ONE);

        TriggeredAbility chapter = (TriggeredAbility) card.getAbilities().getFirst();
        assertThat(card.getAbilities()).hasSize(1);
        assertThat(chapter.effects()).extracting(EffectRegistration::effect).containsExactly(DRAW_ONE, GAIN_ONE);
        assertThat(chapter.chapterTargets()).isEqualTo(new SagaChapterTargets(
                Set.of(TargetFilters.creatureYouControl()), List.of(group), true));
        assertThat(card.getSagaChapterTargetFilters(EffectSlot.SAGA_CHAPTER_I))
                .containsExactly(TargetFilters.creatureYouControl());
        assertThat(card.isSagaChapterTargetRequired(EffectSlot.SAGA_CHAPTER_I)).isTrue();
        assertThat(card.getSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_I)).containsExactly(group);
    }

    @Test
    void aChapterWithoutTargetsHasNone() {
        card.addEffect(EffectSlot.SAGA_CHAPTER_I, DRAW_ONE);

        assertThat(card.getSagaChapterTargetFilters(EffectSlot.SAGA_CHAPTER_I)).isEmpty();
        assertThat(card.getSagaChapterTargetGroups(EffectSlot.SAGA_CHAPTER_II)).isEmpty();
        assertThat(card.isSagaChapterTargetRequired(EffectSlot.SAGA_CHAPTER_I)).isFalse();
    }

    @Test
    void chapterTargetsNeedTheChapterEffectsFirst() {
        assertThatThrownBy(() -> card.setSagaChapterTargetRequired(EffectSlot.SAGA_CHAPTER_II))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SAGA_CHAPTER_II");
    }

    @Test
    void aRuntimeCopyHasTheSameAbilitiesAndChangesOnItsOwn() {
        card.addEffect(EffectSlot.SAGA_CHAPTER_I, DRAW_ONE);
        card.setSagaChapterTargetRequired(EffectSlot.SAGA_CHAPTER_I);
        card.freeze();

        Card copy = card.createRuntimeCopy();
        copy.addEffect(EffectSlot.ON_DEATH, GAIN_ONE);

        assertThat(copy.getAbilities()).startsWith(card.getAbilities().getFirst());
        assertThat(copy.isSagaChapterTargetRequired(EffectSlot.SAGA_CHAPTER_I)).isTrue();
        assertThat(copy.getEffects(EffectSlot.ON_DEATH)).containsExactly(GAIN_ONE);
        assertThat(card.getEffects(EffectSlot.ON_DEATH)).isEmpty();
        assertThat(card.getAbilities()).hasSize(1);
    }

    @Test
    void clearingAbilitiesEmptiesEverySlot() {
        card.addEffect(EffectSlot.SPELL, DRAW_ONE);
        card.addEffect(EffectSlot.STATIC, DRAW_TWO);
        card.addEffect(EffectSlot.ON_DEATH, GAIN_ONE);

        card.clearRulesTextAndAbilities();

        assertThat(card.getAbilities()).isEmpty();
        assertThat(card.getEffects(EffectSlot.SPELL)).isEmpty();
        assertThat(card.getEffects(EffectSlot.STATIC)).isEmpty();
    }

    private static TriggeredAbility legacy(EffectSlot slot, EffectRegistration... effects) {
        return new TriggeredAbility(new LegacySlotTrigger(slot), TriggerOptions.DEFAULT, List.of(effects));
    }
}
