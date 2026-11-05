package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DredgeEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SerumPowderEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * How {@link Card} turns registrations into abilities, and how its views read them back.
 */
class CardAbilitiesTest {

    private static final Trigger LOSES_CONTROL = Triggers.youLoseControlOfThis();

    private static final CardEffect DRAW_ONE = new DrawCardEffect(1);
    private static final CardEffect DRAW_TWO = new DrawCardEffect(2);
    private static final CardEffect GAIN_ONE = new GainLifeEffect(1);
    private static final CardEffect GAIN_TWO = new GainLifeEffect(2);

    private static final CostEffect SACRIFICE_A_CREATURE = new SacrificeCreatureCost();
    private static final CostEffect DELVE = new DelveCost();

    private final Card card = new Card();

    @Test
    void spellEffectsFormOneSpellAbility() {
        card.addSpellEffect(DRAW_ONE);
        card.addSimultaneousSpellEffects(GAIN_ONE, GAIN_TWO);

        assertThat(card.getAbilities()).containsExactly(new SpellAbility(List.of(
                new EffectRegistration(DRAW_ONE),
                new EffectRegistration(GAIN_ONE),
                new EffectRegistration(GAIN_TWO, TriggerMode.NORMAL, true))));
        assertThat(card.getSpellEffects()).containsExactly(DRAW_ONE, GAIN_ONE, GAIN_TWO);
    }

    @Test
    void eachStaticEffectIsAStaticAbilityOfItsZone() {
        card.addStaticEffect(DRAW_ONE);
        card.addStaticEffect(DRAW_TWO);
        card.addStaticEffect(AbilityZones.COMMAND, GAIN_ONE);

        assertThat(card.getAbilities()).containsExactly(
                new StaticAbility(List.of(DRAW_ONE)),
                new StaticAbility(List.of(DRAW_TWO)),
                new StaticAbility(List.of(GAIN_ONE), AbilityZones.COMMAND));
        assertThat(card.getStaticEffects()).containsExactly(DRAW_ONE, DRAW_TWO, GAIN_ONE);
        assertThat(card.getStaticEffects(Zone.BATTLEFIELD)).containsExactly(DRAW_ONE, DRAW_TWO);
        assertThat(card.getStaticEffects(Zone.COMMAND)).containsExactly(GAIN_ONE);
        assertThat(card.getStaticAbilities()).hasSize(3);
    }

    @Test
    void aStaticEffectFunctionsFromTheZonesItsRuleNames() {
        KickerEffect kicker = new KickerEffect("{1}");
        SpliceEffect splice = SpliceEffect.ontoInstantOrSorcery("{1}");
        CardEffect conditionalReduction = new ConditionalEffect(new ControllerLifeAtMost(10),
                new ReduceOwnCastCostEffect(new Fixed(1)));
        card.addStaticEffect(kicker);
        card.addStaticEffect(splice);
        card.addStaticEffect(conditionalReduction);
        card.addStaticEffect(AbilityZones.of(Zone.BATTLEFIELD, Zone.COMMAND), GAIN_ONE);

        assertThat(card.getStaticEffects(Zone.STACK)).containsExactly(kicker, conditionalReduction);
        assertThat(card.getStaticEffects(Zone.HAND)).containsExactly(splice);
        assertThat(card.getStaticEffects(Zone.BATTLEFIELD)).containsExactly(GAIN_ONE);
        assertThat(card.getStaticEffects(Zone.COMMAND)).containsExactly(GAIN_ONE);
        assertThat(card.getStaticEffects()).containsExactly(kicker, splice, conditionalReduction, GAIN_ONE);
    }

    @Test
    void dredgeMulliganAndOpeningHandActionsFunctionFromTheZonesTheirRulesName() {
        DredgeEffect dredge = new DredgeEffect(2);
        SerumPowderEffect powder = new SerumPowderEffect();
        MayEffect leyline = new MayEffect(new LeylineStartOnBattlefieldEffect(), "Begin the game with it?");
        card.addStaticEffect(dredge);
        card.addStaticEffect(powder);
        card.addStaticEffect(leyline);

        assertThat(card.getStaticEffects(Zone.GRAVEYARD)).containsExactly(dredge);
        assertThat(card.getStaticEffects(Zone.HAND)).containsExactly(powder, leyline);
        assertThat(card.getStaticEffects(Zone.BATTLEFIELD)).isEmpty();
        assertThat(card.getTriggeredAbilities()).isEmpty();
    }

    @Test
    void anEntersWithAbilityIsAStaticAbilityOfTheBattlefield() {
        EnterWithCountersEffect counters = new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2));
        card.addStaticEffect(counters);

        assertThat(card.getAbilities()).containsExactly(new StaticAbility(List.of(counters)));
        assertThat(card.getStaticEffects(Zone.BATTLEFIELD)).containsExactly(counters);
        assertThat(card.getTriggerEffects(Triggers.enters(ObjectRelation.THIS))).isEmpty();
    }

    @Test
    void anAddedStaticAbilityKeepsItsZonesAndIsNoTriggeredAbility() {
        card.addStaticEffect(DRAW_ONE);
        card.addAbility(new StaticAbility(List.of(GAIN_ONE, GAIN_TWO), AbilityZones.COMMAND));

        assertThat(card.getAbilities()).containsExactly(
                new StaticAbility(List.of(DRAW_ONE)),
                new StaticAbility(List.of(GAIN_ONE), AbilityZones.COMMAND),
                new StaticAbility(List.of(GAIN_TWO), AbilityZones.COMMAND));
        assertThat(card.getTriggeredAbilities()).isEmpty();
    }

    @Test
    void staticAbilitiesAreCopiedWithTheirZones() {
        Card source = new Card();
        source.addStaticEffect(DRAW_ONE);
        source.addStaticEffect(AbilityZones.COMMAND, GAIN_ONE);
        source.addStaticEffect(DRAW_TWO);

        card.addStaticAbilitiesFrom(source, effect -> effect != DRAW_TWO);

        assertThat(card.getAbilities()).containsExactly(
                new StaticAbility(List.of(DRAW_ONE)),
                new StaticAbility(List.of(GAIN_ONE), AbilityZones.COMMAND));
    }

    @Test
    void eachTriggerRegistrationIsAnAbilityOfItsOwn() {
        card.addTrigger(LOSES_CONTROL, DRAW_ONE);
        card.addTrigger(LOSES_CONTROL, DRAW_TWO);

        assertThat(card.getAbilities()).containsExactly(
                triggered(new EffectRegistration(DRAW_ONE)),
                triggered(new EffectRegistration(DRAW_TWO)));
        assertThat(card.getTriggerEffects(LOSES_CONTROL)).containsExactly(DRAW_ONE, DRAW_TWO);
    }

    @Test
    void simultaneousEffectsStayInOneAbility() {
        card.addTrigger(LOSES_CONTROL, DRAW_ONE);
        card.addSimultaneousTrigger(LOSES_CONTROL, GAIN_ONE, GAIN_TWO);

        assertThat(card.getAbilities()).containsExactly(
                triggered(new EffectRegistration(DRAW_ONE)),
                triggered(new EffectRegistration(GAIN_ONE),
                        new EffectRegistration(GAIN_TWO, TriggerMode.NORMAL, true)));
        assertThat(card.isSimultaneousWithPrevious(GAIN_TWO)).isTrue();
        assertThat(card.isSimultaneousWithPrevious(GAIN_ONE)).isFalse();
    }

    @Test
    void triggerViewsCantBeChangedFromOutside() {
        card.addTrigger(LOSES_CONTROL, DRAW_ONE);

        assertThatThrownBy(() -> card.getTriggerEffectRegistrations(LOSES_CONTROL).clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> card.getAbilities().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(card.getTriggerEffects(Triggers.youGainControlOfThis())).isEmpty();
        assertThat(card.getTriggerEffectRegistrations(Triggers.youGainControlOfThis())).isEmpty();
    }

    @Test
    void viewsFollowEffectsAddedAfterTheyWereRead() {
        card.addTrigger(LOSES_CONTROL, DRAW_ONE);
        assertThat(card.getTriggerEffects(LOSES_CONTROL)).containsExactly(DRAW_ONE);

        card.addTrigger(LOSES_CONTROL, DRAW_TWO);

        assertThat(card.getTriggerEffects(LOSES_CONTROL)).containsExactly(DRAW_ONE, DRAW_TWO);
    }

    @Test
    void removingEffectsDropsAbilitiesLeftEmptyAndKeepsTheOthersAsTheyAre() {
        card.addTrigger(LOSES_CONTROL, DRAW_ONE);
        card.addTrigger(LOSES_CONTROL, GAIN_ONE);
        Ability untouched = card.getAbilities().get(1);

        card.removeEffectsIf(DRAW_ONE::equals);

        assertThat(card.getAbilities()).hasSize(1);
        assertThat(card.getAbilities().getFirst()).isSameAs(untouched);
        assertThat(card.getTriggerEffects(LOSES_CONTROL)).containsExactly(GAIN_ONE);
    }

    @Test
    void anEffectLeftFirstInItsAbilityIsNoLongerSimultaneous() {
        card.addSimultaneousSpellEffects(DRAW_ONE, GAIN_ONE, GAIN_TWO);

        card.removeEffectsIf(DRAW_ONE::equals);

        assertThat(card.getSpellEffectRegistrations()).containsExactly(
                new EffectRegistration(GAIN_ONE), new EffectRegistration(GAIN_TWO, TriggerMode.NORMAL, true));
    }

    @Test
    void effectsCanBeRemovedFromSomeAbilitiesOnly() {
        card.addStaticEffect(DRAW_ONE);
        card.addTrigger(LOSES_CONTROL, DRAW_ONE);

        card.removeAbilityEffectsIf((ability, effect) -> !(ability instanceof StaticAbility));

        assertThat(card.getStaticEffects()).containsExactly(DRAW_ONE);
        assertThat(card.getTriggerEffects(LOSES_CONTROL)).isEmpty();
    }

    @Test
    void sagaChapterTargetsAreHeldByTheChapterAbility() {
        SagaChapterTargetGroup group = new SagaChapterTargetGroup(TargetFilters.creatureAnOpponentControls(), 0, 1);
        card.addTrigger(Triggers.chapter(1), DRAW_ONE);
        card.setSagaChapterTargetFilter(1, Set.of(TargetFilters.creatureYouControl()));
        card.setSagaChapterTargetRequired(1);
        card.setSagaChapterTargetGroups(1, List.of(group));
        card.addTrigger(Triggers.chapter(1), GAIN_ONE);

        TriggeredAbility chapter = (TriggeredAbility) card.getAbilities().getFirst();
        assertThat(card.getAbilities()).hasSize(1);
        assertThat(chapter.effects()).extracting(EffectRegistration::effect).containsExactly(DRAW_ONE, GAIN_ONE);
        assertThat(chapter.chapterTargets()).isEqualTo(new SagaChapterTargets(
                Set.of(TargetFilters.creatureYouControl()), List.of(group), true));
        assertThat(card.getSagaChapterTargetFilters(1))
                .containsExactly(TargetFilters.creatureYouControl());
        assertThat(card.isSagaChapterTargetRequired(1)).isTrue();
        assertThat(card.getSagaChapterTargetGroups(1)).containsExactly(group);
    }

    @Test
    void aChapterWithoutTargetsHasNone() {
        card.addTrigger(Triggers.chapter(1), DRAW_ONE);

        assertThat(card.getSagaChapterTargetFilters(1)).isEmpty();
        assertThat(card.getSagaChapterTargetGroups(2)).isEmpty();
        assertThat(card.isSagaChapterTargetRequired(1)).isFalse();
    }

    @Test
    void chapterTargetsNeedTheChapterEffectsFirst() {
        assertThatThrownBy(() -> card.setSagaChapterTargetRequired(2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no chapter 2 ability");
    }

    @Test
    void aRuntimeCopyHasTheSameAbilitiesAndChangesOnItsOwn() {
        card.addTrigger(Triggers.chapter(1), DRAW_ONE);
        card.setSagaChapterTargetRequired(1);
        card.freeze();

        Card copy = card.createRuntimeCopy();
        copy.addTrigger(LOSES_CONTROL, GAIN_ONE);

        assertThat(copy.getAbilities()).startsWith(card.getAbilities().getFirst());
        assertThat(copy.isSagaChapterTargetRequired(1)).isTrue();
        assertThat(copy.getTriggerEffects(LOSES_CONTROL)).containsExactly(GAIN_ONE);
        assertThat(card.getTriggerEffects(LOSES_CONTROL)).isEmpty();
        assertThat(card.getAbilities()).hasSize(1);
    }

    @Test
    void clearingAbilitiesEmptiesEveryView() {
        card.addSpellEffect(DRAW_ONE);
        card.addStaticEffect(DRAW_TWO);
        card.addTrigger(LOSES_CONTROL, GAIN_ONE);

        card.clearRulesTextAndAbilities();

        assertThat(card.getAbilities()).isEmpty();
        assertThat(card.getSpellEffects()).isEmpty();
        assertThat(card.getStaticEffects()).isEmpty();
    }

    @Test
    void castingCostsAreKeptApartFromTheSpellAbility() {
        card.addCastingCost(SACRIFICE_A_CREATURE);
        card.addSpellEffect(DRAW_ONE);
        card.addCastingCost(DELVE);

        assertThat(card.getAbilities()).containsExactly(new SpellAbility(List.of(new EffectRegistration(DRAW_ONE))));
        assertThat(card.getCastingCosts()).containsExactly(SACRIFICE_A_CREATURE, DELVE);
        assertThat(card.getSpellEffects()).containsExactly(DRAW_ONE);
        assertThat(card.getCastingCostsAndSpellEffects()).containsExactly(SACRIFICE_A_CREATURE, DELVE, DRAW_ONE);
    }

    @Test
    void aCastingCostIsNoSpellEffect() {
        assertThatThrownBy(() -> card.addSpellEffect(SACRIFICE_A_CREATURE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("addCastingCost");
    }

    @Test
    void aCardWithOnlyCastingCostsHasNoSpellAbility() {
        card.addCastingCost(SACRIFICE_A_CREATURE);

        assertThat(card.getSpellAbility()).isEmpty();
        assertThat(card.getAbilities()).isEmpty();
        assertThat(card.getSpellEffects()).isEmpty();
    }

    @Test
    void theSpellAbilityHoldsTheTargetGroupsItsEffectsAreBoundTo() {
        card.target(TargetFilters.creature()).addSpellEffect(DRAW_ONE);
        card.target(TargetFilters.creature(), 0, 2).addSpellEffect(DRAW_TWO);

        SpellAbility spell = card.getSpellAbility().orElseThrow();
        assertThat(spell.targets()).containsExactlyElementsOf(card.getTargetGroups());
        assertThat(spell.targets()).extracting(TargetGroup::getIndex).containsExactly(0, 1);
        assertThat(spell.targets().get(1).getMaxTargets()).isEqualTo(2);
        assertThat(card.getSpellTargetGroups()).isEqualTo(spell.targets());
    }

    @Test
    void groupsOfOtherAbilitiesAfterTheSpellsStayOnTheCardOnly() {
        // Death Pulse: the spell's target, then its cycling trigger's.
        card.target(TargetFilters.creature()).addSpellEffect(DRAW_ONE);
        card.target(TargetFilters.creature()).addTrigger(Triggers.youCycleThisCard(), DRAW_TWO);

        assertThat(card.getTargetGroups()).hasSize(2);
        assertThat(card.getSpellTargetGroups()).containsExactly(card.getTargetGroups().getFirst());
    }

    @Test
    void aGroupNoEffectIsBoundToIsOneOfTheSpells() {
        // A positional target a sibling effect reads by index (Blood Feud's first fight target).
        card.target(TargetFilters.creature());
        card.target(TargetFilters.creature()).addSpellEffect(DRAW_ONE);

        assertThat(card.getSpellTargetGroups()).hasSize(2);
    }

    @Test
    void aGroupDeclaredBeforeTheSpellEffectJoinsTheSpellWhenTheEffectArrives() {
        SpellTarget group = card.target(TargetFilters.creature());

        group.addSpellEffect(DRAW_ONE);
        group.inDistinctScope(3);

        assertThat(card.getSpellAbility().orElseThrow().targets())
                .singleElement()
                .extracting(TargetGroup::getDistinctScope)
                .isEqualTo(3);
    }

    @Test
    void aCardWithoutASpellAbilityOffersAllItsGroupsAsSpellTargets() {
        // An Aura spell: its Enchant ability decides its target.
        card.target(TargetFilters.creature()).addStaticEffect(GAIN_ONE);

        assertThat(card.getSpellAbility()).isEmpty();
        assertThat(card.getSpellTargetGroups()).isEqualTo(card.getTargetGroups()).hasSize(1);
    }

    @Test
    void aRuntimeCopyKeepsTheCastingCostsAndTheSpellsTargets() {
        card.addCastingCost(SACRIFICE_A_CREATURE);
        card.target(TargetFilters.creature()).addSpellEffect(DRAW_ONE);

        Card copy = card.createRuntimeCopy();
        copy.addCastingCost(DELVE);

        assertThat(copy.getCastingCosts()).containsExactly(SACRIFICE_A_CREATURE, DELVE);
        assertThat(card.getCastingCosts()).containsExactly(SACRIFICE_A_CREATURE);
        assertThat(copy.getSpellTargetGroups()).isEqualTo(card.getSpellTargetGroups()).hasSize(1);
    }

    @Test
    void removingEffectsReachesCastingCostsToo() {
        card.addCastingCost(SACRIFICE_A_CREATURE);
        card.addSpellEffect(DRAW_ONE);

        card.removeEffectsIf(SACRIFICE_A_CREATURE::equals);

        assertThat(card.getCastingCosts()).isEmpty();
        assertThat(card.getSpellEffects()).containsExactly(DRAW_ONE);
    }

    @Test
    void anotherCardsCastingCostsAndSpellEffectsCanBeCopiedSelectively() {
        Card source = new Card();
        source.addCastingCost(SACRIFICE_A_CREATURE);
        source.addSimultaneousSpellEffects(DRAW_ONE, GAIN_ONE);

        card.addCastingCostsAndSpellEffectsFrom(source, effect -> effect != GAIN_ONE);

        assertThat(card.getCastingCosts()).containsExactly(SACRIFICE_A_CREATURE);
        assertThat(card.getSpellEffectRegistrations()).containsExactly(new EffectRegistration(DRAW_ONE));
    }

    private static TriggeredAbility triggered(EffectRegistration... effects) {
        return new TriggeredAbility(LOSES_CONTROL, TriggerOptions.DEFAULT, List.of(effects));
    }

    @Test
    void activatedAbilitiesAreOneListReadByZone() {
        ActivatedAbility mana = ManaAbilities.tapFor(ManaColor.GREEN);
        ActivatedAbility cycling = new ActivatedAbility(false, "{2}", List.of(DRAW_ONE), "Cycling {2}");
        ActivatedAbility unearth = Card.unearthAbility("{B}");
        ActivatedAbility fromExile = new ActivatedAbility(false, "{3}", List.of(DRAW_ONE), "From exile")
                .withZones(AbilityZones.EXILE);
        card.addActivatedAbility(mana);
        card.addActivatedAbility(AbilityZones.HAND, cycling);
        card.addActivatedAbility(unearth);
        card.addActivatedAbility(fromExile);

        assertThat(card.getActivatedAbilities()).containsExactly(mana, cycling, unearth, fromExile);
        assertThat(card.getActivatedAbilities(Zone.BATTLEFIELD)).containsExactly(mana);
        assertThat(card.getActivatedAbilities(Zone.HAND)).containsExactly(cycling);
        assertThat(card.getActivatedAbilities(Zone.GRAVEYARD)).containsExactly(unearth);
        assertThat(card.getActivatedAbilities(Zone.EXILE)).containsExactly(fromExile);
        assertThat(card.getActivatedAbilities(Zone.STACK)).isEmpty();
    }

    @Test
    void anActivatedAbilityCanFunctionFromSeveralZones() {
        ActivatedAbility both = new ActivatedAbility(false, "{1}", List.of(DRAW_ONE), "Both")
                .withZones(AbilityZones.of(Zone.BATTLEFIELD, Zone.GRAVEYARD));
        card.addActivatedAbility(both);

        assertThat(card.getActivatedAbilities(Zone.BATTLEFIELD)).containsExactly(both);
        assertThat(card.getActivatedAbilities(Zone.GRAVEYARD)).containsExactly(both);
    }

    @Test
    void activatedAbilityViewsFollowChangesAndCantBeChangedFromOutside() {
        ActivatedAbility first = ManaAbilities.tapFor(ManaColor.RED);
        card.addActivatedAbility(first);
        List<ActivatedAbility> before = card.getActivatedAbilities(Zone.BATTLEFIELD);

        ActivatedAbility second = ManaAbilities.tapFor(ManaColor.BLUE);
        card.addActivatedAbility(second);
        assertThat(card.getActivatedAbilities(Zone.BATTLEFIELD)).containsExactly(first, second);
        assertThat(before).containsExactly(first);

        card.removeActivatedAbilitiesIf(ability -> ability == first);
        assertThat(card.getActivatedAbilities(Zone.BATTLEFIELD)).containsExactly(second);

        card.clearActivatedAbilities();
        assertThat(card.getActivatedAbilities()).isEmpty();
        assertThatThrownBy(() -> card.getActivatedAbilities().add(first))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> card.getActivatedAbilities(Zone.BATTLEFIELD).add(first))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void activatedAbilitiesAreCopiedWithTheirZonesAndTheCopyChangesAlone() {
        ActivatedAbility mana = ManaAbilities.tapFor(ManaColor.GREEN);
        ActivatedAbility cycling = new ActivatedAbility(false, "{2}", List.of(DRAW_ONE), "Cycling {2}");
        card.addActivatedAbility(mana);
        card.addActivatedAbility(AbilityZones.HAND, cycling);
        card.getActivatedAbilities(Zone.BATTLEFIELD);

        Card copy = new Card(card);
        copy.addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));

        assertThat(copy.getActivatedAbilities(Zone.HAND)).containsExactly(cycling);
        assertThat(copy.getActivatedAbilities(Zone.BATTLEFIELD)).hasSize(2);
        assertThat(card.getActivatedAbilities(Zone.BATTLEFIELD)).containsExactly(mana);
    }

    @Test
    void aTriggerIsATriggeredAbilityOfItsOwnReadOnlyByItsTrigger() {
        card.addTrigger(LOSES_CONTROL, DRAW_ONE);
        card.addTrigger(Triggers.dies(ObjectRelation.THIS), DRAW_TWO, GAIN_ONE);

        TriggeredAbility added = card.getTriggeredAbilities().get(1);
        assertThat(added.conditions()).containsExactly(new TriggerCondition(Triggers.dies(ObjectRelation.THIS)));
        assertThat(added.options()).isEqualTo(TriggerOptions.DEFAULT);
        assertThat(added.effects()).extracting(EffectRegistration::effect).containsExactly(DRAW_TWO, GAIN_ONE);
        assertThat(card.getTriggeredAbilities()).hasSize(2);
        assertThat(card.getTriggerEffects(LOSES_CONTROL)).containsExactly(DRAW_ONE);
    }

    @Test
    void aTriggerKeepsItsConditionZonesAndOptions() {
        TriggerCondition fromExile = new TriggerCondition(Triggers.dies(ObjectRelation.THIS), AbilityZones.EXILE);
        TriggerOptions oncePerTurn = TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN);

        card.addTrigger(fromExile, oncePerTurn, DRAW_ONE);

        TriggeredAbility added = card.getTriggeredAbilities().getFirst();
        assertThat(added.conditions()).containsExactly(fromExile);
        assertThat(added.options()).isEqualTo(oncePerTurn);
    }

    @Test
    void addingATriggeredAbilityWithARealTriggerAddsItAsItIs() {
        TriggeredAbility ability = new TriggeredAbility(Triggers.leavesBattlefield(ObjectRelation.THIS),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(DRAW_ONE),
                new EffectRegistration(GAIN_ONE, TriggerMode.NORMAL, true)));

        card.addAbility(ability);

        assertThat(card.getTriggeredAbilities()).containsExactly(ability);
        assertThat(card.createRuntimeCopy().getTriggeredAbilities()).containsExactly(ability);
    }

    @Test
    void theOwnEntryEffectsWrapEachEffectInItsAbilitysInterveningIfOnce() {
        ControllerLifeAtMost lowLife = new ControllerLifeAtMost(10);
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), DRAW_ONE);
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerOptions.DEFAULT.withInterveningIf(lowLife),
                GAIN_ONE);

        List<CardEffect> ownEntry = card.getOwnEntryEffects();

        assertThat(ownEntry).containsExactly(DRAW_ONE, new ConditionalEffect(lowLife, GAIN_ONE, true));
        assertThat(ownEntry.getFirst()).isSameAs(DRAW_ONE);
        // Built once with the index, so the wrapped effect keeps its identity across reads, by ability too.
        assertThat(card.getOwnEntryEffects().get(1)).isSameAs(ownEntry.get(1));
        assertThat(card.getOwnEntryEffectRegistrations(card.getOwnEntryAbilities().get(1)).getFirst().effect())
                .isSameAs(ownEntry.get(1));
        assertThat(card.getTriggerEffects(Triggers.enters(ObjectRelation.THIS))).containsExactly(DRAW_ONE, GAIN_ONE);
    }

    @Test
    void theOwnEntryEffectsLeaveALimitToTheCollector() {
        TriggeredAbility ability = new TriggeredAbility(List.of(
                new TriggerCondition(Triggers.enters(ObjectRelation.THIS)),
                new TriggerCondition(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL))),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), List.of(new EffectRegistration(DRAW_ONE)));
        card.addTriggeredAbility(ability);

        assertThat(card.getOwnEntryAbilities()).containsExactly(ability);
        assertThat(card.getOwnEntryEffects()).containsExactly(DRAW_ONE);
        assertThat(card.getOwnEntryEffectRegistrations()).extracting(EffectRegistration::effect)
                .containsExactlyElementsOf(card.getOwnEntryEffects());
    }
}
