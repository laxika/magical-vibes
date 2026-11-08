package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.service.trigger.LifeAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.LifeWatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/** The life gain, loss and payment triggers: {@link LifeTrigger}. */
class LifeTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);
    private static final CardPredicate WHITE = new CardColorPredicate(CardColor.WHITE);

    @Test
    void everyLifeTriggerFunctionsFromTheBattlefieldByDefault() {
        assertThat(allFactories()).allSatisfy(trigger ->
                assertThat(trigger.defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD));
    }

    @Test
    void everyFactoryNamesItsOwnTrigger() {
        assertThat(allFactories()).doesNotHaveDuplicates();
    }

    @Test
    void namesTheEventAndWhoseLifeTotalItIs() {
        assertThat(Triggers.youGainLife())
                .isEqualTo(new LifeTrigger(LifeTrigger.LifeEvent.GAINS, PlayerRelation.YOU));
        assertThat(Triggers.opponentGainsLife())
                .isEqualTo(new LifeTrigger(LifeTrigger.LifeEvent.GAINS, PlayerRelation.OPPONENT));
        assertThat(Triggers.playerGainsLife())
                .isEqualTo(new LifeTrigger(LifeTrigger.LifeEvent.GAINS, PlayerRelation.ANY));
        assertThat(Triggers.youLoseLife())
                .isEqualTo(new LifeTrigger(LifeTrigger.LifeEvent.LOSES, PlayerRelation.YOU));
        assertThat(Triggers.opponentLosesLife())
                .isEqualTo(new LifeTrigger(LifeTrigger.LifeEvent.LOSES, PlayerRelation.OPPONENT));
        assertThat(Triggers.youPayLife())
                .isEqualTo(new LifeTrigger(LifeTrigger.LifeEvent.PAYS, PlayerRelation.YOU))
                .isNotEqualTo(Triggers.youLoseLife());
    }

    @Test
    void needsAnEventAndAPlayer() {
        assertThatThrownBy(() -> new LifeTrigger(null, PlayerRelation.YOU)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LifeTrigger(LifeTrigger.LifeEvent.GAINS, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsLifeAbilitiesByTriggerAndZone() {
        Card card = new Card();
        card.addTrigger(Triggers.youGainLife(), DRAW);
        card.addTrigger(new TriggerCondition(Triggers.youGainLife(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                GAIN);

        assertThat(card.getTriggerEffects(Triggers.youGainLife(), Zone.BATTLEFIELD)).containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.youGainLife(), Zone.GRAVEYARD)).containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.opponentGainsLife(), Zone.BATTLEFIELD)).isEmpty();
        assertThat(card.getTriggerEffects(Triggers.youLoseLife(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void theFirstTimeEachTurnIsDroppedFromTheWatchKey() {
        LifeTrigger first = Triggers.youGainLife().firstTimeEachTurn();

        assertThat(first.firstEachTurn()).isTrue();
        assertThat(first).isNotEqualTo(Triggers.youGainLife());
        assertThat(first.watchKey()).isEqualTo(Triggers.youGainLife());
        assertThat(Triggers.youGainLife().watchKey()).isEqualTo(Triggers.youGainLife());
        assertThatThrownBy(first::firstTimeEachTurn).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theSpellFilterIsDroppedFromTheWatchKeyAndReadFromTheAbility() {
        LifeTrigger whiteSpell = Triggers.youGainLife().causedBySpell(WHITE);
        Card card = new Card();
        card.addTrigger(whiteSpell, DRAW);

        assertThat(whiteSpell.watchKey()).isEqualTo(Triggers.youGainLife());
        assertThat(LifeAbilityReader.readPrinted(card, LifeWatch.ON_CONTROLLER_GAINS_LIFE)).singleElement()
                .satisfies(read -> {
                    assertThat(read.effect()).isSameAs(DRAW);
                    assertThat(read.spellFilter()).isEqualTo(WHITE);
                });
        assertThatThrownBy(() -> LifeAbilityReader.printedEffects(card, LifeWatch.ON_CONTROLLER_GAINS_LIFE))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("spell filter");
        assertThatThrownBy(() -> Triggers.youLoseLife().causedBySpell(WHITE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> whiteSpell.causedBySpell(WHITE)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LifeAbilityReader.read(new TriggeredAbility(
                Triggers.opponentGainsLife().causedBySpell(WHITE), DRAW), Triggers.opponentGainsLife()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anOpponentsFirstLossDuringTheirTurnIsWatchedByAnOpponentLosesLife() {
        LifeTrigger theirTurn = Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn();

        assertThat(theirTurn.watchKey()).isEqualTo(Triggers.opponentLosesLife());
        assertThatThrownBy(theirTurn::firstTimeEachTurn).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aLifeAbilityHasNoLegacyView() {
        Card card = new Card();
        card.addTrigger(Triggers.youGainLife(), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();

        assertThat(ability.hasOwnEntry()).isFalse();
        assertThatThrownBy(ability::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LifeWatch.ON_CONTROLLER_GAINS_LIFE.effectsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> LifeWatch.ON_CONTROLLER_GAINS_LIFE.registrationsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(card.getTriggerEffects(Triggers.youGainLife(), Zone.BATTLEFIELD)).containsExactly(DRAW);
    }

    @Test
    void theReaderGivesTheFirstTimeEachTurnAndWrapsTheInterveningIfAroundTheEffect() {
        Card card = new Card();
        card.addTrigger(Triggers.youGainLife().firstTimeEachTurn(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), DRAW);

        assertThat(LifeAbilityReader.readPrinted(card, LifeWatch.ON_CONTROLLER_GAINS_LIFE)).singleElement()
                .satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(new ConditionalEffect(new ControllerTurn(), DRAW));
                    assertThat(read.event()).isEqualTo(LifeTrigger.LifeEvent.GAINS);
                    assertThat(read.firstTimeEachTurn()).isTrue();
                    assertThat(read.limit()).isEqualTo(TriggerLimit.NONE);
                    assertThat(read.triggersOnlyOnceEachTurn()).isFalse();
                });
        assertThat(card.getTriggerEffects(Triggers.youGainLife(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void theReaderWrapsOnlyTheInterveningIfAndLeavesTheEventConditionToTheCollector() {
        Card card = new Card();
        card.addTrigger(Triggers.opponentGainsLife(), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerTurn()).withEventCondition(new TargetPlayerTurn()), DRAW);

        assertThat(LifeAbilityReader.printedEffects(card, LifeWatch.ON_OPPONENT_GAINS_LIFE))
                .containsExactly(new ConditionalEffect(new ControllerTurn(),
                        DRAW));
    }

    @Test
    void aGraveyardCountIsReadInTheGraveyard() {
        Card card = new Card();
        card.addTrigger(new TriggerCondition(Triggers.youGainLife().firstTimeEachTurn(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, DRAW);

        assertThat(LifeAbilityReader.readPrinted(card, LifeWatch.GRAVEYARD_ON_CONTROLLER_GAINS_LIFE)).singleElement()
                .satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(DRAW);
                    assertThat(read.firstTimeEachTurn()).isTrue();
                });
        assertThat(LifeAbilityReader.printed(card, LifeWatch.ON_CONTROLLER_GAINS_LIFE)).isEmpty();
    }

    @Test
    void oneAbilityWithAGainAndALossConditionSharesItsLimit() {
        Card card = new Card();
        card.addTrigger(List.of(Triggers.youGainLife(), Triggers.youLoseLife()),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()).withLimit(TriggerLimit.ONCE_EACH_TURN),
                DRAW);
        LifeAbilityReader.LifeEffect gain =
                LifeAbilityReader.readPrinted(card, LifeWatch.ON_CONTROLLER_GAINS_LIFE).getFirst();
        LifeAbilityReader.LifeEffect loss =
                LifeAbilityReader.readPrinted(card, LifeWatch.ON_CONTROLLER_LOSES_LIFE).getFirst();
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        gain.markLimit(gameData, sourceId);

        assertThat(gain.ability()).isSameAs(loss.ability());
        assertThat(loss.effect()).isEqualTo(new ConditionalEffect(new ControllerTurn(), DRAW));
        assertThat(loss.limitReached(gameData, sourceId)).isTrue();
        assertThat(loss.triggersOnlyOnceEachTurn()).isTrue();
        assertThat(gameData.keyedOncePerTurnTriggersFiredThisTurn.get(sourceId))
                .containsExactly(gain.ability().limitKey());
        assertThat(gameData.oncePerTurnTriggersFiredThisTurn).doesNotContain(sourceId);
    }

    @Test
    void doThisOnlyOnceEachTurnIsMarkedAsTheActionIsTaken() {
        Card card = new Card();
        card.addTrigger(Triggers.opponentLosesLife(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN), DRAW);
        LifeAbilityReader.LifeEffect read =
                LifeAbilityReader.readPrinted(card, LifeWatch.ON_OPPONENT_LOSES_LIFE).getFirst();
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        read.markLimit(gameData, sourceId);

        assertThat(read.marksLimitOnAcceptance()).isTrue();
        assertThat(read.triggersOnlyOnceEachTurn()).isTrue();
        assertThat(read.limitReached(gameData, sourceId)).isFalse();
        gameData.oncePerTurnTriggersFiredThisTurn.add(sourceId);
        assertThat(read.limitReached(gameData, sourceId)).isTrue();
        assertThatThrownBy(() -> LifeAbilityReader.requireNoLimitOnAcceptance(read,
                LifeWatch.ON_OPPONENT_LOSES_LIFE.trigger())).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anOpponentsFirstLossDuringTheirTurnIsReadWithEveryEffectByAnOpponentLosesLife() {
        Card card = new Card();
        card.addTrigger(Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn(), DRAW, GAIN);

        assertThat(LifeAbilityReader.readPrinted(card, LifeWatch.ON_OPPONENT_LOSES_LIFE))
                .extracting(LifeAbilityReader.LifeEffect::effect, LifeAbilityReader.LifeEffect::event)
                .containsExactly(tuple(DRAW, LifeTrigger.LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN),
                        tuple(GAIN, LifeTrigger.LifeEvent.LOSES_FOR_FIRST_TIME_DURING_THEIR_TURN));
        assertThatThrownBy(() -> LifeAbilityReader.printedEffects(card, LifeWatch.ON_OPPONENT_LOSES_LIFE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anEntersAndLifeGainAbilityIsReadAsItsEffectsForTheOwnEntryAndSharesItsLimit() {
        Card card = new Card();
        card.addTrigger(List.of(Triggers.enters(ObjectRelation.THIS), Triggers.youGainLife()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        assertThat(ability.hasOwnEntry()).isTrue();
        assertThat(card.getOwnEntryEffects()).containsExactly(DRAW);
        LifeAbilityReader.LifeEffect read =
                LifeAbilityReader.readPrinted(card, LifeWatch.ON_CONTROLLER_GAINS_LIFE).getFirst();
        assertThat(read.effect()).isEqualTo(DRAW);
        read.markLimit(gameData, sourceId);
        assertThat(gameData.keyedOncePerTurnTriggersFiredThisTurn.get(sourceId))
                .containsExactly(ability.limitKey());
    }

    @Test
    void theStrictReaderRejectsACountALimitOrANarrowerEvent() {
        Card counted = new Card();
        counted.addTrigger(Triggers.youGainLife().firstTimeEachTurn(), DRAW);
        Card limited = new Card();
        limited.addTrigger(Triggers.opponentGainsLife(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        Card plain = new Card();
        plain.addTrigger(Triggers.opponentGainsLife(), DRAW);

        assertThatThrownBy(() -> LifeAbilityReader.printedEffects(counted, LifeWatch.ON_CONTROLLER_GAINS_LIFE))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LifeAbilityReader.printedRegistrations(limited, LifeWatch.ON_OPPONENT_GAINS_LIFE))
                .isInstanceOf(IllegalStateException.class);
        assertThat(LifeAbilityReader.printedRegistrations(plain, LifeWatch.ON_OPPONENT_GAINS_LIFE))
                .containsExactly(new EffectRegistration(DRAW));
    }

    @Test
    void whatTheLifeCollectorsCantReadIsRejected() {
        assertThatThrownBy(() -> new TriggeredAbility(Triggers.youGainLife(), TriggerOptions.ONE_OR_MORE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> read(Triggers.opponentGainsLife().firstTimeEachTurn(), TriggerOptions.DEFAULT,
                LifeWatch.ON_OPPONENT_GAINS_LIFE)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> read(Triggers.youPayLife().firstTimeEachTurn(), TriggerOptions.DEFAULT,
                LifeWatch.ON_CONTROLLER_PAYS_LIFE)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> read(Triggers.youGainLife().firstTimeEachTurn(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), LifeWatch.ON_CONTROLLER_GAINS_LIFE))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> read(Triggers.youGainLife(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE),
                LifeWatch.ON_CONTROLLER_GAINS_LIFE)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> read(Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), LifeWatch.ON_OPPONENT_LOSES_LIFE))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LifeAbilityReader.read(new TriggeredAbility(List.of(
                        new TriggerCondition(Triggers.youGainLife()),
                        new TriggerCondition(Triggers.youGainLife().firstTimeEachTurn())), TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(DRAW))), Triggers.youGainLife()))
                .isInstanceOf(IllegalStateException.class);
    }

    private static List<LifeAbilityReader.LifeEffect> read(LifeTrigger trigger, TriggerOptions options,
                                                           LifeWatch watch) {
        return LifeAbilityReader.read(new TriggeredAbility(trigger, options, DRAW), watch.trigger());
    }

    private static List<LifeTrigger> allFactories() {
        return Stream.of(Triggers.youGainLife(), Triggers.opponentGainsLife(), Triggers.playerGainsLife(),
                Triggers.youLoseLife(), Triggers.opponentLosesLife(),
                Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn(), Triggers.youPayLife()).toList();
    }
}
