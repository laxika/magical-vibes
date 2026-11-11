package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.SourceIsFaceDown;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToMatchingCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.trigger.TargetAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.TargetWatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/** The becomes-the-target triggers: {@link TargetTrigger}. */
class TargetTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect SACRIFICE = new SacrificeSelfEffect();
    private static final CardEffect WARD = new CounterUnlessPaysEffect(2);
    private static final PermanentPredicate DRAGON = new PermanentHasSubtypePredicate(CardSubtype.DRAGON);

    @Test
    void functionsFromTheBattlefield() {
        assertThat(Triggers.becomesTargetOfOpponentSpellOrAbility().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.youBecomeTargetOfSpell().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.playerChoosesTargets().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void namesWhatTargetsAndWhatBecomesTheTarget() {
        assertThat(Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility())
                .isEqualTo(new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                        ObjectRelation.YOU_CONTROL, Triggers.CREATURE))
                .isNotEqualTo(Triggers.permanentYouControlBecomesTargetOfOpponentSpellOrAbility())
                .isNotEqualTo(Triggers.anotherPermanentYouControlBecomesTargetOfOpponentSpellOrAbility())
                .isNotEqualTo(Triggers.creatureOrCreatureSpellYouControlBecomesTargetOfOpponentSpellOrAbility())
                .isNotEqualTo(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility())
                .isNotEqualTo(Triggers.creatureYouControlBecomesTargetOfOpponentSpell());
        assertThat(Triggers.becomesTargetOfOpponentSpellOrAbility())
                .isEqualTo(new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                        ObjectRelation.THIS))
                .isNotEqualTo(Triggers.becomesTargetOfOpponentSpell())
                .isNotEqualTo(Triggers.becomesTargetOfSpellOrAbility());
        assertThat(Triggers.becomesTargetOfSpell())
                .isNotEqualTo(Triggers.becomesTargetOfAuraSpell())
                .isNotEqualTo(Triggers.creatureYouControlBecomesTargetOfSpell());
        assertThat(Triggers.creatureYouControlBecomesTargetOfSpell())
                .isNotEqualTo(Triggers.creatureYouControlBecomesTargetOfInstantOrSorcerySpell())
                .isNotEqualTo(Triggers.creatureYouControlBecomesTargetOfBackupAbility())
                .isNotEqualTo(Triggers.creaturesYouControlBecomeTargetOfActivatedAbility());
        assertThat(Triggers.creatureBecomesTargetOfSpellOrAbility())
                .isNotEqualTo(Triggers.anotherCreatureYouControlBecomesTargetOfSpellOrAbility())
                .isNotEqualTo(Triggers.creatureOpponentControlsBecomesTargetOfYourSpellOrAbility());
        assertThat(Triggers.youBecomeTargetOfSpell()).isNotEqualTo(Triggers.youBecomeTargetOfOpponentSpellOrAbility());
    }

    @Test
    void aFilterIsDroppedFromTheWatchKey() {
        TargetTrigger dragon = Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility().withFilter(DRAGON);

        assertThat(dragon.qualifier()).isEqualTo(DRAGON);
        assertThat(dragon.filter()).isEqualTo(Triggers.CREATURE);
        assertThat(dragon.watchKey()).isEqualTo(Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility());
        assertThat(Triggers.becomesTargetOfSpell().watchKey()).isEqualTo(Triggers.becomesTargetOfSpell());
        assertThatThrownBy(() -> dragon.withFilter(DRAGON)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Triggers.becomesTargetOfSpell().withFilter(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void theEventsWithoutACollectorOfTheirOwnAreWatchedByTheEventAboutAnyone() {
        assertThat(Triggers.becomesTargetOfYourSpell().watchKey()).isEqualTo(Triggers.becomesTargetOfSpell());
        assertThat(Triggers.becomesTargetOfYourSpellOrAbility().watchKey())
                .isEqualTo(Triggers.becomesTargetOfSpellOrAbility());
        assertThat(Triggers.playerOrPermanentBecomesTargetOfYourAbility().watchKey())
                .isEqualTo(Triggers.playerChoosesTargets());
        assertThat(Triggers.creatureOpponentControlsBecomesTargetOfYourSpellOrAbility().watchKey())
                .isEqualTo(Triggers.creatureOpponentControlsBecomesTargetOfYourSpellOrAbility());
        assertThat(Triggers.becomesTargetOfOpponentSpellOrAbility().watchKey())
                .isEqualTo(Triggers.becomesTargetOfOpponentSpellOrAbility());
    }

    @Test
    void aTargetAbilityHasNoLegacyView() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesTargetOfSpellOrAbility(), SACRIFICE);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(ability);

        assertThatThrownBy(ability::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY.effectsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY.registrationsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void theReaderGivesYourSpellOrAbilityTheFilterAndTheLimitAndWrapsTheInterveningIfAroundTheEffect() {
        Card valiant = new Card();
        valiant.addTrigger(Triggers.becomesTargetOfYourSpellOrAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility ability = valiant.getTriggeredAbilities().getFirst();
        Card dragon = new Card();
        dragon.addTrigger(Triggers.creatureYouControlBecomesTargetOfOpponentSpellOrAbility().withFilter(DRAGON),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), WARD);

        assertThat(TargetAbilityReader.readPrinted(valiant, TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY))
                .singleElement().satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(DRAW);
                    assertThat(read.ability()).isSameAs(ability);
                    assertThat(read.filter()).isNull();
                    assertThat(read.byController()).isTrue();
                    assertThat(read.limit()).isEqualTo(TriggerLimit.ONCE_EACH_TURN);
                });
        assertThat(TargetAbilityReader.readPrinted(dragon,
                TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY))
                .singleElement().satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(new ConditionalEffect(new ControllerTurn(), WARD));
                    assertThat(read.filter()).isEqualTo(DRAGON);
                    assertThat(read.byController()).isFalse();
                    assertThat(read.limit()).isEqualTo(TriggerLimit.NONE);
                });
    }

    @Test
    void theOtherNarrowedEventsAreReadByTheWatchOfTheEventAboutAnyone() {
        Card yourSpell = new Card();
        yourSpell.addTrigger(Triggers.becomesTargetOfYourSpell(), DRAW);
        Card yourAbility = new Card();
        yourAbility.addTrigger(Triggers.playerOrPermanentBecomesTargetOfYourAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        Card anyone = new Card();
        anyone.addTrigger(Triggers.playerChoosesTargets(), SACRIFICE);

        assertThat(TargetAbilityReader.readPrinted(yourSpell, TargetWatch.ON_BECOMES_TARGET_OF_SPELL))
                .extracting(TargetAbilityReader.TargetEffect::effect, TargetAbilityReader.TargetEffect::byController)
                .containsExactly(tuple(DRAW, true));
        assertThat(TargetAbilityReader.readPrinted(yourAbility, TargetWatch.ON_ANY_PLAYER_CHOOSES_TARGETS))
                .extracting(TargetAbilityReader.TargetEffect::effect, TargetAbilityReader.TargetEffect::byController,
                        TargetAbilityReader.TargetEffect::limit)
                .containsExactly(tuple(DRAW, true, TriggerLimit.ONCE_EACH_TURN));
        assertThat(TargetAbilityReader.readPrinted(anyone, TargetWatch.ON_ANY_PLAYER_CHOOSES_TARGETS))
                .extracting(TargetAbilityReader.TargetEffect::effect, TargetAbilityReader.TargetEffect::byController)
                .containsExactly(tuple(SACRIFICE, false));
    }

    @Test
    void theReaderWrapsOnlyTheInterveningIfAndLeavesTheEventConditionToTheCollector() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerTurn()).withEventCondition(new SourceIsFaceDown()), WARD);

        assertThat(TargetAbilityReader.printedEffects(card, TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL))
                .containsExactly(new ConditionalEffect(new ControllerTurn(),
                        WARD));
    }

    @Test
    void triggersOnlyOnceEachTurnIsMarkedByTheAbilityOnItsSource() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesTargetOfYourSpellOrAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        card.addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), SACRIFICE);
        List<TargetAbilityReader.TargetEffect> reads =
                TargetAbilityReader.readPrinted(card, TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY);
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        reads.getFirst().markLimit(gameData, sourceId);

        assertThat(reads.getFirst().limitReached(gameData, sourceId)).isTrue();
        assertThat(reads.getFirst().limitReached(gameData, UUID.randomUUID())).isFalse();
        assertThat(reads.get(1).limitReached(gameData, sourceId)).isFalse();
    }

    @Test
    void theStrictReaderRejectsAFilterANarrowerEventOrALimit() {
        Card filtered = new Card();
        filtered.addTrigger(Triggers.creatureYouControlBecomesTargetOfSpell().withFilter(DRAGON), DRAW);
        Card narrower = new Card();
        narrower.addTrigger(Triggers.becomesTargetOfYourSpell(), DRAW);
        Card limited = new Card();
        limited.addTrigger(Triggers.becomesTargetOfAuraSpell(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        Card plain = new Card();
        plain.addTrigger(Triggers.becomesTargetOfAuraSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), DRAW);

        assertThatThrownBy(() -> TargetAbilityReader.printedEffects(filtered,
                TargetWatch.ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TargetAbilityReader.printedEffects(narrower, TargetWatch.ON_BECOMES_TARGET_OF_SPELL))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TargetAbilityReader.printedEffects(limited,
                TargetWatch.ON_BECOMES_TARGET_OF_AURA_SPELL)).isInstanceOf(IllegalStateException.class);
        assertThat(TargetAbilityReader.printedEffects(plain, TargetWatch.ON_BECOMES_TARGET_OF_AURA_SPELL))
                .containsExactly(new ConditionalEffect(new ControllerTurn(), DRAW));
    }

    @Test
    void aMultiplicityALimitOrTwoReadingsTheTargetCollectorsCantReadAreRejected() {
        Card doOnce = new Card();
        doOnce.addTrigger(Triggers.becomesTargetOfSpellOrAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN), DRAW);
        Card twoReadings = new Card();
        twoReadings.addTrigger(List.of(Triggers.becomesTargetOfYourSpell(), Triggers.becomesTargetOfSpell()), DRAW);

        assertThatThrownBy(() -> new Card().addTrigger(Triggers.becomesTargetOfSpell(), TriggerOptions.ONE_OR_MORE,
                DRAW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TargetAbilityReader.readPrinted(doOnce,
                TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> TargetAbilityReader.readPrinted(twoReadings, TargetWatch.ON_BECOMES_TARGET_OF_SPELL))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedAbilitiesAreFoundByTheWatchedTriggerAndReadWithTheirLimit() {
        TriggeredAbility granted = new TriggeredAbility(Triggers.becomesTargetOfSpellOrAbility(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(), WARD));

        assertThat(granted.hasWatchedTrigger(TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY.trigger())).isTrue();
        assertThat(TargetAbilityReader.read(granted, TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY.trigger()))
                .singleElement().satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(DRAW);
                    assertThat(read.limit()).isEqualTo(TriggerLimit.ONCE_EACH_TURN);
                });
        assertThat(TargetAbilityReader.grantedToPermanentEffects(permanent,
                TargetWatch.ON_BECOMES_TARGET_OF_OPPONENT_SPELL)).containsExactly(WARD);
        assertThat(TargetAbilityReader.grantedToPermanentEffects(permanent,
                TargetWatch.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY)).isEmpty();
    }

    @Test
    void needsAnEventAndARelation() {
        assertThatThrownBy(() -> new TargetTrigger(null, ObjectRelation.THIS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TargetTrigger(TargetTrigger.TargetEvent.BECOMES_TARGET_OF_SPELL, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsTargetAbilitiesByTrigger() {
        Card card = new Card();
        card.addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), WARD);
        card.addTrigger(Triggers.becomesTargetOfSpellOrAbility(), SACRIFICE);

        assertThat(card.getTriggerEffects(Triggers.becomesTargetOfOpponentSpellOrAbility(), Zone.BATTLEFIELD))
                .containsExactly(WARD);
        assertThat(card.getTriggerEffects(Triggers.becomesTargetOfSpellOrAbility(), Zone.BATTLEFIELD))
                .containsExactly(SACRIFICE);
        assertThat(card.getTriggerEffects(Triggers.becomesTargetOfOpponentSpellOrAbility(), Zone.GRAVEYARD)).isEmpty();
        assertThat(card.getTriggerEffects(Triggers.becomesTargetOfSpell(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void aPerpetualWardGrantIsReadByItsTrigger() {
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID cardId = UUID.randomUUID();
        gameData.grantPerpetualAbility(cardId, new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(), WARD));

        assertThat(gameData.perpetualTriggerEffects(cardId, Triggers.becomesTargetOfOpponentSpellOrAbility(),
                Zone.BATTLEFIELD)).containsExactly(WARD);
        assertThat(gameData.perpetualTriggerEffects(cardId, Triggers.becomesTargetOfOpponentSpell(),
                Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void thePerpetualKeywordGrantsTakeTheWardAbility() {
        TriggeredAbility ward = new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(), WARD);

        PerpetuallyGrantKeywordsToTargetCreatureEffect toTarget =
                new PerpetuallyGrantKeywordsToTargetCreatureEffect(Set.of(Keyword.WARD), ward);
        assertThat(toTarget.grantedAbility()).isEqualTo(ward);
        assertThat(toTarget.triggeredAbility()).isEqualTo(WARD);
        assertThat(toTarget.filter()).isInstanceOf(PermanentControlledBySourceControllerPredicate.class);

        PerpetuallyGrantKeywordToMatchingCardsEffect toMatching =
                new PerpetuallyGrantKeywordToMatchingCardsEffect(null, null, Set.of(Keyword.WARD), ward);
        assertThat(toMatching.grantedAbility()).isEqualTo(ward);
        assertThat(toMatching.graveyardFilter()).isNull();
        assertThat(toMatching.libraryFilter()).isNull();
    }
}
