package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnPerCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.trigger.TapWatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The tap and untap triggers: {@link TapTrigger}. */
class TapTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);
    private static final PermanentPredicate MERFOLK = new PermanentHasSubtypePredicate(CardSubtype.MERFOLK);
    private static final PermanentPredicate SOURCE = new PermanentIsSourceCardPredicate();

    @Test
    void everyTapTriggerFunctionsFromTheBattlefield() {
        assertThat(allFactories()).allSatisfy(trigger ->
                assertThat(trigger.defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD));
    }

    @Test
    void everyFactoryNamesItsOwnTrigger() {
        assertThat(allFactories()).doesNotHaveDuplicates();
    }

    @Test
    void namesTheEventThePermanentsRelationAndItsType() {
        assertThat(Triggers.tappedForMana())
                .isEqualTo(new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA, ObjectRelation.THIS));
        assertThat(Triggers.playerTapsLandForMana())
                .isEqualTo(new TapTrigger(TapTrigger.TapEvent.TAPPED_FOR_MANA, ObjectRelation.ANY, Triggers.LAND))
                .isNotEqualTo(Triggers.playerTapsPermanentForMana());
        assertThat(Triggers.youTapArtifactTokenForMana().filter()).isEqualTo(Triggers.ARTIFACT_TOKEN);
        assertThat(Triggers.youTapCreatureForMana().relation()).isEqualTo(ObjectRelation.YOU_CONTROL);
        assertThat(Triggers.enchantedPermanentBecomesTapped().relation()).isEqualTo(ObjectRelation.ENCHANTED);
        assertThat(Triggers.permanentOpponentControlsBecomesTapped())
                .isEqualTo(new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, ObjectRelation.OPPONENT_CONTROLS))
                .isNotEqualTo(Triggers.youTapPermanentOpponentControls());
        assertThat(Triggers.creatureBecomesTapped())
                .isEqualTo(new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, ObjectRelation.ANY, Triggers.CREATURE));
        assertThat(Triggers.becomesUntapped())
                .isNotEqualTo(Triggers.permanentYouControlBecomesUntapped())
                .isNotEqualTo(Triggers.permanentBecomesUntapped());
        assertThat(Triggers.youUntapPermanentsDuringUntapStep().event())
                .isEqualTo(TapTrigger.TapEvent.YOU_UNTAP_DURING_UNTAP_STEP);
    }

    @Test
    void needsAnEventAndARelation() {
        assertThatThrownBy(() -> new TapTrigger(null, ObjectRelation.THIS)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TapTrigger(TapTrigger.TapEvent.BECOMES_TAPPED, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsTapAbilitiesByTrigger() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesUntapped(), DRAW);
        card.addTrigger(Triggers.permanentYouControlBecomesTapped(), GAIN);

        assertThat(card.getTriggerEffects(Triggers.becomesUntapped(), Zone.BATTLEFIELD)).containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.permanentYouControlBecomesTapped(), Zone.BATTLEFIELD))
                .containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.permanentBecomesUntapped(), Zone.BATTLEFIELD)).isEmpty();
        assertThat(card.getTriggerEffects(Triggers.becomesUntapped(), Zone.GRAVEYARD)).isEmpty();
    }

    @Test
    void aRegistrationKeepsItsTriggerMode() {
        Card card = new Card();
        card.addTrigger(Triggers.permanentYouControlBecomesTapped(), TriggerMode.ONCE_PER_BATCH, DRAW);

        assertThat(card.getTriggerEffectRegistrations(Triggers.permanentYouControlBecomesTapped(), Zone.BATTLEFIELD))
                .extracting(EffectRegistration::triggerMode)
                .containsExactly(TriggerMode.ONCE_PER_BATCH);
    }

    @Test
    void aFilterIsDroppedFromTheWatchKey() {
        TapTrigger merfolk = Triggers.permanentYouControlBecomesTapped().withFilter(MERFOLK);

        assertThat(merfolk.qualifier()).isEqualTo(MERFOLK);
        assertThat(merfolk.watchKey()).isEqualTo(Triggers.permanentYouControlBecomesTapped());
        assertThat(Triggers.youTapCreatureForMana().watchKey()).isEqualTo(Triggers.youTapCreatureForMana());
        assertThatThrownBy(() -> merfolk.withFilter(MERFOLK)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Triggers.becomesTapped().withFilter(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void thisPermanentAndTheFirstTimeDuringYourTurnAreWatchedByAPermanentYouControl() {
        assertThat(Triggers.becomesTapped().narrowed()).isTrue();
        assertThat(Triggers.becomesTapped().withFilter(MERFOLK).watchKey())
                .isEqualTo(Triggers.permanentYouControlBecomesTapped());
        assertThat(Triggers.becomesTappedForFirstTimeDuringYourTurn().watchKey())
                .isEqualTo(Triggers.permanentYouControlBecomesTapped());
        assertThat(Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn().watchKey())
                .isEqualTo(Triggers.permanentYouControlBecomesTapped());
        assertThat(Triggers.becomesUntapped().narrowed()).isFalse();
        assertThat(Triggers.becomesUntapped().watchKey()).isEqualTo(Triggers.becomesUntapped());
        assertThat(Triggers.permanentYouControlBecomesTapped().narrowed()).isFalse();
    }

    @Test
    void theLegacyViewGivesThisPermanentOutsideTheLimitAndTheInterveningIfInsideIt() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesTapped(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()).withLimit(TriggerLimit.ONCE_EACH_TURN),
                DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();

        List<CardEffect> legacy = TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.effectsOf(card);

        assertThat(legacy).containsExactly(new TriggeringPermanentConditionalEffect(SOURCE,
                OncePerTurnTriggerEffect.keyed(new ConditionalEffect(new ControllerTurn(), DRAW),
                        ability.limitKey())));
        assertThat(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.effectsOf(card).getFirst()).isSameAs(legacy.getFirst());
        assertThat(card.getTriggerEffects(Triggers.permanentYouControlBecomesTapped(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void theLegacyViewJoinsThisPermanentAndTheFilter() {
        PermanentPredicate minusCounter = new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE);
        Card thisFiltered = new Card();
        thisFiltered.addTrigger(Triggers.becomesTapped().withFilter(minusCounter), DRAW);
        Card merfolk = new Card();
        merfolk.addTrigger(Triggers.permanentOpponentControlsBecomesTapped().withFilter(MERFOLK), DRAW);

        assertThat(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.effectsOf(thisFiltered))
                .containsExactly(new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(SOURCE, minusCounter)), DRAW));
        assertThat(TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED.effectsOf(merfolk))
                .containsExactly(new TriggeringPermanentConditionalEffect(MERFOLK, DRAW));
    }

    @Test
    void theLegacyViewGivesTheFirstTimeDuringYourTurnInsideTheFilter() {
        Card self = new Card();
        self.addTrigger(Triggers.becomesTappedForFirstTimeDuringYourTurn(), DRAW);
        Card creature = new Card();
        creature.addTrigger(Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn(), DRAW);

        assertThat(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.effectsOf(self))
                .containsExactly(new TriggeringPermanentConditionalEffect(SOURCE,
                        new OncePerTurnPerCreatureTriggerEffect(DRAW, true)));
        assertThat(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.effectsOf(creature))
                .containsExactly(new TriggeringPermanentConditionalEffect(Triggers.CREATURE,
                        new OncePerTurnPerCreatureTriggerEffect(DRAW, true)));
    }

    @Test
    void oneOrMoreIsTheBatchedTriggerMode() {
        Card card = new Card();
        card.addTrigger(Triggers.permanentYouControlBecomesTapped().withFilter(MERFOLK), TriggerOptions.ONE_OR_MORE,
                DRAW);

        assertThat(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.registrationsOf(card))
                .containsExactly(new EffectRegistration(new TriggeringPermanentConditionalEffect(MERFOLK, DRAW),
                        TriggerMode.ONCE_PER_BATCH));
    }

    @Test
    void oneAbilityOnAPermanentYouControlOrAnOpponentControlsIsReadByBothWatches() {
        Card card = new Card();
        card.addTrigger(List.of(Triggers.permanentYouControlBecomesTapped().withFilter(MERFOLK),
                Triggers.permanentOpponentControlsBecomesTapped().withFilter(MERFOLK)), DRAW);
        CardEffect filtered = new TriggeringPermanentConditionalEffect(MERFOLK, DRAW);

        assertThat(card.getTriggeredAbilities()).hasSize(1);
        assertThat(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.effectsOf(card)).containsExactly(filtered);
        assertThat(TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED.effectsOf(card)).containsExactly(filtered);
    }

    @Test
    void whatTheTapCollectorsCantReadIsRejected() {
        Card manaFilter = new Card();
        manaFilter.addTrigger(Triggers.tappedForMana().withFilter(MERFOLK), DRAW);
        Card untapOneOrMore = new Card();
        untapOneOrMore.addTrigger(Triggers.becomesUntapped(), TriggerOptions.ONE_OR_MORE, DRAW);
        Card firstTimeLimited = new Card();
        firstTimeLimited.addTrigger(Triggers.becomesTappedForFirstTimeDuringYourTurn(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        Card twoModes = new Card();
        twoModes.addTrigger(Triggers.permanentYouControlBecomesTapped(), TriggerMode.ONCE_PER_BATCH,
                TriggerOptions.ONE_OR_MORE, DRAW);
        Card twoFilters = new Card();
        twoFilters.addTrigger(List.of(Triggers.permanentYouControlBecomesTapped().withFilter(MERFOLK),
                Triggers.permanentOpponentControlsBecomesTapped()), DRAW);

        assertThatThrownBy(() -> TapWatch.ON_SELF_TAPPED_FOR_MANA.effectsOf(manaFilter))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapWatch.ON_SELF_BECOMES_UNTAPPED.effectsOf(untapOneOrMore))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.effectsOf(firstTimeLimited))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.effectsOf(twoModes))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.effectsOf(twoFilters))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedAbilitiesAreReadInTheLegacyForm() {
        TriggeredAbility granted = new TriggeredAbility(Triggers.becomesTapped(), DRAW);
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(granted);
        permanent.addPersistentAbility(granted);
        CardEffect legacy = new TriggeringPermanentConditionalEffect(SOURCE, DRAW);

        assertThat(granted.hasWatchedTrigger(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger())).isTrue();
        assertThat(permanent.getLegacyTemporaryTriggerEffects(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger()))
                .containsExactly(legacy);
        assertThat(permanent.getLegacyPersistentTriggerEffects(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger()))
                .containsExactly(legacy);
    }

    private static List<TapTrigger> allFactories() {
        return Stream.of(Triggers.tappedForMana(), Triggers.playerTapsLandForMana(),
                Triggers.playerTapsPermanentForMana(), Triggers.youTapCreatureForMana(),
                Triggers.youTapNonlandPermanentForMana(), Triggers.youTapArtifactTokenForMana(),
                Triggers.enchantedPermanentBecomesTapped(), Triggers.becomesTapped(),
                Triggers.becomesTappedForFirstTimeDuringYourTurn(),
                Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn(),
                Triggers.permanentYouControlBecomesTapped(),
                Triggers.permanentOpponentControlsBecomesTapped(), Triggers.creatureBecomesTapped(),
                Triggers.youTapPermanentOpponentControls(), Triggers.tappedToPayTeamworkCost(),
                Triggers.becomesUntapped(), Triggers.permanentYouControlBecomesUntapped(),
                Triggers.permanentBecomesUntapped(), Triggers.youUntapPermanentsDuringUntapStep()).toList();
    }
}
