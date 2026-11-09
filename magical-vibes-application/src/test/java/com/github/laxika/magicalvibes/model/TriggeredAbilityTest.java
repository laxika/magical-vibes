package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriggeredAbilityTest {

    @Test
    void onlyACastTriggeredAbilityNamesItsOwnTargetFilter() {
        TargetFilter creature = TargetFilters.creature();
        TriggeredAbility cast = new TriggeredAbility(Triggers.youCastSpell().anySpell(), new DrawCardEffect(1))
                .withTargetFilter(creature);

        assertThat(cast.targetFilter()).isSameAs(creature);
        assertThat(cast.withEffects(cast.effects()).targetFilter()).isSameAs(creature);
        assertThatThrownBy(() -> new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU), new DrawCardEffect(1))
                .withTargetFilter(creature))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own target filter");
    }

    @Test
    void aYourUpkeepAbilityNamesItsOwnTargetFilterForTheYourUpkeepReader() {
        TargetFilter creature = TargetFilters.creature();
        TriggeredAbility upkeep = new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), new DrawCardEffect(1))
                .withTargetFilter(creature);

        assertThat(upkeep.targetFilter()).isSameAs(creature);
        assertThatThrownBy(() -> new TriggeredAbility(Triggers.upkeep(ActivePlayer.ANY), new DrawCardEffect(1))
                .withTargetFilter(creature))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own target filter");
    }

    @Test
    void aYouGainLifeAbilityNamesItsOwnTargetFilterForTheEmblemReader() {
        TargetFilter creature = TargetFilters.creature();
        TriggeredAbility lifeGain = new TriggeredAbility(Triggers.youGainLife(), new DrawCardEffect(1))
                .withTargetFilter(creature);

        assertThat(lifeGain.targetFilter()).isSameAs(creature);
        assertThatThrownBy(() -> new TriggeredAbility(Triggers.opponentGainsLife(), new DrawCardEffect(1))
                .withTargetFilter(creature))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own target filter");
    }

    private static final List<EffectRegistration> ZOMBIE =
            List.of(new EffectRegistration(CreateTokenEffect.blackZombie(1)));

    @Test
    void aSingleTriggerFunctionsFromItsDefaultZones() {
        Trigger trigger = Triggers.chaosEnsues();

        TriggeredAbility ability = new TriggeredAbility(trigger, TriggerOptions.DEFAULT, ZOMBIE);

        assertThat(ability.conditions()).containsExactly(new TriggerCondition(trigger, AbilityZones.COMMAND));
        assertThat(ability.effects()).isEqualTo(ZOMBIE);
    }

    @Test
    void keepsEffectsThatHappenAtTheSameTimeAsTheOneBefore() {
        EffectRegistration first = new EffectRegistration(new DrawCardEffect(1));
        EffectRegistration simultaneous = new EffectRegistration(new DrawCardEffect(2), true);

        TriggeredAbility ability = new TriggeredAbility(Triggers.youLoseControlOfThis(),
                TriggerOptions.DEFAULT, List.of(first, simultaneous));

        assertThat(ability.effects()).containsExactly(first, simultaneous);
    }

    @Test
    void holdsUnmodifiableCopiesOfItsConditionsAndEffects() {
        List<TriggerCondition> conditions =
                new ArrayList<>(List.of(new TriggerCondition(Triggers.youLoseControlOfThis())));
        List<EffectRegistration> effects = new ArrayList<>(ZOMBIE);
        TriggeredAbility ability = new TriggeredAbility(conditions, TriggerOptions.DEFAULT, effects);

        conditions.clear();
        effects.clear();

        assertThat(ability.conditions()).hasSize(1);
        assertThat(ability.effects()).isEqualTo(ZOMBIE);
        assertThatThrownBy(() -> ability.effects().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void needsATriggerConditionAndAnEffect() {
        Trigger trigger = Triggers.youLoseControlOfThis();

        assertThatThrownBy(() -> new TriggeredAbility(List.of(), TriggerOptions.DEFAULT, ZOMBIE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TriggeredAbility(trigger, TriggerOptions.DEFAULT, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TriggeredAbility(trigger, null, ZOMBIE))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void firstEffectCannotBeSimultaneousWithAPreviousOne() {
        EffectRegistration simultaneous = new EffectRegistration(new DrawCardEffect(1), true);

        assertThatThrownBy(() -> new TriggeredAbility(Triggers.youLoseControlOfThis(),
                TriggerOptions.DEFAULT, List.of(simultaneous)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anInstanceOfAnAbilityKeepsItsOwnLimitKeyAndTheWithersKeepTheInstance() {
        TriggeredAbility ability = new TriggeredAbility(Triggers.thisManaAbilityResolves(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), new DrawCardEffect(1));

        TriggeredAbility fromOne = ability.withInstance("granted-by:one");
        TriggeredAbility fromTwo = ability.withInstance("granted-by:two");

        assertThat(ability.instance()).isNull();
        assertThat(fromOne).isNotEqualTo(fromTwo);
        assertThat(fromOne.limitKey()).isNotEqualTo(fromTwo.limitKey()).isNotEqualTo(ability.limitKey());
        assertThat(ability.withInstance("granted-by:one").limitKey()).isEqualTo(fromOne.limitKey());
        assertThat(fromOne.withEffects(ZOMBIE).instance()).isEqualTo("granted-by:one");
        assertThat(fromOne.withChapterTargets(SagaChapterTargets.NONE)).isEqualTo(fromOne);
    }

    @Test
    void aSpellLandPlayOrActivationAbilityHasNoLegacyView() {
        TriggeredAbility cast = new TriggeredAbility(Triggers.youCastSpell(), new DrawCardEffect(1));
        TriggeredAbility landPlay = new TriggeredAbility(Triggers.youPlayLand(), new DrawCardEffect(1));
        TriggeredAbility activation = new TriggeredAbility(Triggers.youActivateAbility(), new DrawCardEffect(1));
        TriggeredAbility enters = new TriggeredAbility(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));

        assertThatThrownBy(cast::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(landPlay::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(activation::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThat(enters.ownEntryEffects()).isEqualTo(enters.effects());
    }

    @Test
    void aStepAbilityHasNoLegacyViewAndIsFoundByTriggerAndZone() {
        Card card = new Card();
        card.addTrigger(new TriggerCondition(Triggers.upkeep(ActivePlayer.YOU), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()), new DrawCardEffect(1));
        TriggeredAbility upkeep = card.getTriggeredAbilities().getFirst();

        assertThatThrownBy(upkeep::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThat(card.getTriggeredAbilities(Triggers.upkeep(ActivePlayer.YOU), Zone.GRAVEYARD))
                .containsExactly(upkeep);
        assertThat(card.getTriggeredAbilities(Triggers.upkeep(ActivePlayer.YOU), Zone.BATTLEFIELD)).isEmpty();
        assertThat(card.getTriggeredAbilities(Triggers.upkeep(ActivePlayer.YOU))).containsExactly(upkeep);
        assertThat(card.getTriggeredAbilities(Triggers.endStep(ActivePlayer.YOU))).isEmpty();
    }
}
