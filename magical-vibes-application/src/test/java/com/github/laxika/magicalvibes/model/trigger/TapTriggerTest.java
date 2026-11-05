package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.DuringYourUntapStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.trigger.TapAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.TapWatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

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
    void aTapAbilityHasNoLegacyView() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesTapped(), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(ability);

        assertThatThrownBy(ability::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.effectsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.registrationsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(card.getTriggerEffects(Triggers.becomesTapped(), Zone.BATTLEFIELD)).containsExactly(DRAW);
    }

    @Test
    void theReaderGivesThisPermanentAndTheLimitAndWrapsTheInterveningIfAroundTheEffect() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesTapped(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()).withLimit(TriggerLimit.ONCE_EACH_TURN),
                DRAW);

        assertThat(TapAbilityReader.readPrinted(card, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .singleElement().satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(new ConditionalEffect(new ControllerTurn(), DRAW));
                    assertThat(read.filter()).isEqualTo(SOURCE);
                    assertThat(read.firstTime()).isFalse();
                    assertThat(read.oneOrMore()).isFalse();
                    assertThat(read.limit()).isEqualTo(TriggerLimit.ONCE_EACH_TURN);
                });
        assertThat(card.getTriggerEffects(Triggers.permanentYouControlBecomesTapped(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void theReaderWrapsOnlyTheInterveningIfAndLeavesTheEventConditionToTheCollector() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesUntapped(), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerTurn()).withEventCondition(new DuringYourUntapStep()), DRAW);

        assertThat(TapAbilityReader.printedEffects(card, TapWatch.ON_SELF_BECOMES_UNTAPPED))
                .containsExactly(new ConditionalEffect(new ControllerTurn(),
                        DRAW));
    }

    @Test
    void theReaderJoinsThisPermanentAndTheFilter() {
        PermanentPredicate minusCounter = new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE);
        Card thisFiltered = new Card();
        thisFiltered.addTrigger(Triggers.becomesTapped().withFilter(minusCounter), DRAW);
        Card merfolk = new Card();
        merfolk.addTrigger(Triggers.permanentOpponentControlsBecomesTapped().withFilter(MERFOLK), DRAW);

        assertThat(TapAbilityReader.readPrinted(thisFiltered, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .extracting(TapAbilityReader.TapEffect::effect, TapAbilityReader.TapEffect::filter)
                .containsExactly(tuple(DRAW, new PermanentAllOfPredicate(List.of(SOURCE, minusCounter))));
        assertThat(TapAbilityReader.readPrinted(merfolk, TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED))
                .extracting(TapAbilityReader.TapEffect::effect, TapAbilityReader.TapEffect::filter)
                .containsExactly(tuple(DRAW, MERFOLK));
    }

    @Test
    void theFirstTimeDuringYourTurnIsReadWithThisPermanentOrTheType() {
        Card self = new Card();
        self.addTrigger(Triggers.becomesTappedForFirstTimeDuringYourTurn(), DRAW);
        Card creature = new Card();
        creature.addTrigger(Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn(), DRAW);

        assertThat(TapAbilityReader.readPrinted(self, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .extracting(TapAbilityReader.TapEffect::effect, TapAbilityReader.TapEffect::filter,
                        TapAbilityReader.TapEffect::firstTime)
                .containsExactly(tuple(DRAW, SOURCE, true));
        assertThat(TapAbilityReader.readPrinted(creature, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .extracting(TapAbilityReader.TapEffect::effect, TapAbilityReader.TapEffect::filter,
                        TapAbilityReader.TapEffect::firstTime)
                .containsExactly(tuple(DRAW, Triggers.CREATURE, true));
    }

    @Test
    void oneOrMoreIsReadFromTheAbility() {
        Card card = new Card();
        card.addTrigger(Triggers.permanentYouControlBecomesTapped().withFilter(MERFOLK), TriggerOptions.ONE_OR_MORE,
                DRAW);

        assertThat(TapAbilityReader.readPrinted(card, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .extracting(TapAbilityReader.TapEffect::effect, TapAbilityReader.TapEffect::filter,
                        TapAbilityReader.TapEffect::oneOrMore)
                .containsExactly(tuple(DRAW, MERFOLK, true));
    }

    @Test
    void theLimitIsMarkedByTheAbilityOnItsSource() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesTapped(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        card.addTrigger(Triggers.permanentYouControlBecomesTapped(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), GAIN);
        List<TapAbilityReader.TapEffect> reads =
                TapAbilityReader.readPrinted(card, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED);
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        reads.getFirst().markLimit(gameData, sourceId);

        assertThat(reads.getFirst().limitReached(gameData, sourceId)).isTrue();
        assertThat(reads.getFirst().limitReached(gameData, UUID.randomUUID())).isFalse();
        assertThat(reads.get(1).limitReached(gameData, sourceId)).isFalse();
        assertThat(gameData.oncePerTurnTriggersFiredThisTurn).doesNotContain(sourceId);
    }

    @Test
    void oneAbilityOnAPermanentYouControlOrAnOpponentControlsIsReadByBothWatches() {
        Card card = new Card();
        card.addTrigger(List.of(Triggers.permanentYouControlBecomesTapped().withFilter(MERFOLK),
                Triggers.permanentOpponentControlsBecomesTapped().withFilter(MERFOLK)), DRAW);

        assertThat(card.getTriggeredAbilities()).hasSize(1);
        assertThat(TapAbilityReader.readPrinted(card, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .extracting(TapAbilityReader.TapEffect::effect, TapAbilityReader.TapEffect::filter)
                .containsExactly(tuple(DRAW, MERFOLK));
        assertThat(TapAbilityReader.readPrinted(card, TapWatch.ON_OPPONENT_PERMANENT_BECOMES_TAPPED))
                .extracting(TapAbilityReader.TapEffect::effect, TapAbilityReader.TapEffect::filter)
                .containsExactly(tuple(DRAW, MERFOLK));
    }

    @Test
    void theStrictReaderRejectsAFilterTheFirstTimeOneOrMoreOrALimit() {
        Card filtered = new Card();
        filtered.addTrigger(Triggers.permanentYouControlBecomesTapped().withFilter(MERFOLK), DRAW);
        Card firstTime = new Card();
        firstTime.addTrigger(Triggers.creatureYouControlBecomesTappedForFirstTimeDuringYourTurn(), DRAW);
        Card oneOrMore = new Card();
        oneOrMore.addTrigger(Triggers.permanentYouControlBecomesTapped(), TriggerOptions.ONE_OR_MORE, DRAW);
        Card limited = new Card();
        limited.addTrigger(Triggers.permanentBecomesUntapped(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        Card plain = new Card();
        plain.addTrigger(Triggers.permanentBecomesUntapped(), DRAW);

        assertThatThrownBy(() -> TapAbilityReader.printedEffects(filtered, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapAbilityReader.printedEffects(firstTime, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapAbilityReader.printedEffects(oneOrMore, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapAbilityReader.printedRegistrations(limited,
                TapWatch.ON_ANY_PERMANENT_BECOMES_UNTAPPED)).isInstanceOf(IllegalStateException.class);
        assertThat(TapAbilityReader.printedRegistrations(plain, TapWatch.ON_ANY_PERMANENT_BECOMES_UNTAPPED))
                .containsExactly(new EffectRegistration(DRAW));
    }

    @Test
    void whatTheTapCollectorsCantReadIsRejected() {
        Card manaFilter = new Card();
        manaFilter.addTrigger(Triggers.tappedForMana().withFilter(MERFOLK), DRAW);
        Card firstTimeLimited = new Card();
        firstTimeLimited.addTrigger(Triggers.becomesTappedForFirstTimeDuringYourTurn(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        Card onceOnly = new Card();
        onceOnly.addTrigger(Triggers.becomesTapped(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE), DRAW);
        Card twoFilters = new Card();
        twoFilters.addTrigger(List.of(Triggers.becomesTapped(),
                Triggers.permanentYouControlBecomesTapped().withFilter(MERFOLK)), DRAW);

        assertThatThrownBy(() -> new Card().addTrigger(Triggers.becomesUntapped(), TriggerOptions.ONE_OR_MORE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.becomesTapped(), TriggerOptions.ONE_OR_MORE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TapAbilityReader.readPrinted(manaFilter, TapWatch.ON_SELF_TAPPED_FOR_MANA))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapAbilityReader.readPrinted(firstTimeLimited,
                TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapAbilityReader.readPrinted(onceOnly, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TapAbilityReader.readPrinted(twoFilters, TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedAbilitiesAreFoundByTheWatchedTriggerAndReadAsPrinted() {
        TriggeredAbility granted = new TriggeredAbility(Triggers.becomesTapped(), DRAW);
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(granted);
        permanent.addPersistentAbility(granted);

        assertThat(granted.hasWatchedTrigger(TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger())).isTrue();
        assertThat(TapAbilityReader.readGrantedToPermanent(permanent,
                TapWatch.ON_ALLY_PERMANENT_BECOMES_TAPPED.trigger()))
                .extracting(TapAbilityReader.TapEffect::effect, TapAbilityReader.TapEffect::filter)
                .containsExactly(tuple(DRAW, SOURCE), tuple(DRAW, SOURCE));
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
