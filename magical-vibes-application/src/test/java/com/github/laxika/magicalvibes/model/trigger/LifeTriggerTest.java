package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
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

    private static List<LifeTrigger> allFactories() {
        return Stream.of(Triggers.youGainLife(), Triggers.opponentGainsLife(), Triggers.playerGainsLife(),
                Triggers.youLoseLife(), Triggers.opponentLosesLife(), Triggers.youPayLife()).toList();
    }
}
