package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.FirstOpponentLifeLossEachTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.service.trigger.LifeWatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The life gain, loss and payment triggers: {@link LifeTrigger}. */
class LifeTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);

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
    void anOpponentsFirstLossDuringTheirTurnIsWatchedByAnOpponentLosesLife() {
        LifeTrigger theirTurn = Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn();

        assertThat(theirTurn.watchKey()).isEqualTo(Triggers.opponentLosesLife());
        assertThatThrownBy(theirTurn::firstTimeEachTurn).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theLegacyViewGivesTheFirstTimeEachTurnOutsideTheInterveningIf() {
        Card gain = new Card();
        gain.addTrigger(Triggers.youGainLife().firstTimeEachTurn(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), DRAW);
        TriggeredAbility gainAbility = gain.getTriggeredAbilities().getFirst();
        Card loss = new Card();
        loss.addTrigger(Triggers.youLoseLife().firstTimeEachTurn(), DRAW);

        List<CardEffect> legacy = LifeWatch.ON_CONTROLLER_GAINS_LIFE.effectsOf(gain);

        assertThat(legacy).containsExactly(new OncePerTurnTriggerEffect(
                new ConditionalEffect(new ControllerTurn(), DRAW), false, gainAbility.limitKey(), false, true));
        assertThat(LifeWatch.ON_CONTROLLER_GAINS_LIFE.effectsOf(gain).getFirst()).isSameAs(legacy.getFirst());
        assertThat(LifeWatch.ON_CONTROLLER_LOSES_LIFE.effectsOf(loss))
                .containsExactly(OncePerTurnTriggerEffect.firstLifeLoss(DRAW));
        assertThat(gain.getTriggerEffects(Triggers.youGainLife(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void theLegacyViewReadsAGraveyardFirstTimeEachTurnInTheGraveyard() {
        Card card = new Card();
        card.addTrigger(new TriggerCondition(Triggers.youGainLife().firstTimeEachTurn(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, DRAW);

        assertThat(card.getLegacyTriggerEffects(LifeWatch.GRAVEYARD_ON_CONTROLLER_GAINS_LIFE.trigger(),
                Zone.GRAVEYARD)).singleElement().isInstanceOfSatisfying(OncePerTurnTriggerEffect.class, once -> {
                    assertThat(once.firstLifeGain()).isTrue();
                    assertThat(once.wrapped()).isEqualTo(DRAW);
                });
        assertThat(LifeWatch.ON_CONTROLLER_GAINS_LIFE.effectsOf(card)).isEmpty();
    }

    @Test
    void oneAbilityWithAGainAndALossConditionSharesItsLimit() {
        Card card = new Card();
        card.addTrigger(List.of(Triggers.youGainLife(), Triggers.youLoseLife()),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()).withLimit(TriggerLimit.ONCE_EACH_TURN),
                DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();
        CardEffect expected = OncePerTurnTriggerEffect.keyed(new ConditionalEffect(new ControllerTurn(), DRAW),
                ability.limitKey());

        assertThat(LifeWatch.ON_CONTROLLER_GAINS_LIFE.effectsOf(card)).containsExactly(expected);
        assertThat(LifeWatch.ON_CONTROLLER_LOSES_LIFE.effectsOf(card)).containsExactly(expected);
    }

    @Test
    void theLegacyViewMakesAnOpponentsFirstLossDuringTheirTurnOneRecordOfEveryEffect() {
        Card card = new Card();
        card.addTrigger(Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn(), DRAW, GAIN);

        assertThat(LifeWatch.ON_OPPONENT_LOSES_LIFE.effectsOf(card))
                .containsExactly(new FirstOpponentLifeLossEachTurnTriggerEffect(List.of(DRAW, GAIN)));
    }

    @Test
    void theLegacyViewRejectsWhatNoLifeCollectorReads() {
        assertThatThrownBy(() -> legacy(Triggers.opponentGainsLife().firstTimeEachTurn(), TriggerOptions.DEFAULT))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> legacy(Triggers.youPayLife().firstTimeEachTurn(), TriggerOptions.DEFAULT))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> legacy(Triggers.youGainLife().firstTimeEachTurn(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> legacy(Triggers.youGainLife(), TriggerOptions.ONE_OR_MORE))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> legacy(Triggers.youGainLife(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> legacy(Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new TriggeredAbility(List.of(new TriggerCondition(Triggers.youGainLife()),
                new TriggerCondition(Triggers.youLoseLife().firstTimeEachTurn())), TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(DRAW))).legacyEffects())
                .isInstanceOf(IllegalStateException.class);
    }

    private static List<?> legacy(LifeTrigger trigger, TriggerOptions options) {
        return new TriggeredAbility(trigger, options, DRAW).legacyEffects();
    }

    private static List<LifeTrigger> allFactories() {
        return Stream.of(Triggers.youGainLife(), Triggers.opponentGainsLife(), Triggers.playerGainsLife(),
                Triggers.youLoseLife(), Triggers.opponentLosesLife(),
                Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn(), Triggers.youPayLife()).toList();
    }
}
