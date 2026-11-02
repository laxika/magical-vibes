package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StepTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);

    @Test
    void functionsFromTheBattlefieldUnlessGivenOtherZones() {
        assertThat(Triggers.upkeep(ActivePlayer.YOU).defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(new TriggerCondition(Triggers.endStep(ActivePlayer.ANY)).zones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void namesTheStepAndWhoseItIs() {
        assertThat(Triggers.upkeep(ActivePlayer.OPPONENT))
                .isEqualTo(new StepTrigger(StepTrigger.Moment.UPKEEP, ActivePlayer.OPPONENT))
                .isNotEqualTo(Triggers.upkeep(ActivePlayer.ANY))
                .isNotEqualTo(Triggers.drawStep(ActivePlayer.OPPONENT));
        assertThat(Triggers.endOfCombat()).isEqualTo(new StepTrigger(StepTrigger.Moment.END_OF_COMBAT, ActivePlayer.ANY));
        assertThat(Triggers.secondMainPhase(ActivePlayer.YOU))
                .isNotEqualTo(Triggers.postcombatMainPhase(ActivePlayer.YOU));
    }

    @Test
    void needsAMomentAndAPlayer() {
        assertThatThrownBy(() -> new StepTrigger(null, ActivePlayer.YOU)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new StepTrigger(StepTrigger.Moment.UPKEEP, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsTheAbilitiesOfOneTriggerByTheZoneTheyFunctionFrom() {
        Card card = new Card();
        StepTrigger yourUpkeep = Triggers.upkeep(ActivePlayer.YOU);
        card.addTrigger(yourUpkeep, DRAW);
        card.addTrigger(new TriggerCondition(yourUpkeep, AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT, GAIN);
        card.addTrigger(Triggers.upkeep(ActivePlayer.ANY), GAIN);

        assertThat(card.getTriggerEffects(yourUpkeep, Zone.BATTLEFIELD)).containsExactly(DRAW);
        assertThat(card.getTriggerEffects(yourUpkeep, Zone.GRAVEYARD)).containsExactly(GAIN);
        assertThat(card.getTriggerEffects(yourUpkeep, Zone.EXILE)).isEmpty();
        assertThat(card.getTriggerEffects(yourUpkeep)).containsExactly(DRAW, GAIN);
    }

    @Test
    void aPerpetualGrantIsReadByTheZoneItFunctionsFrom() {
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID cardId = UUID.randomUUID();
        StepTrigger yourUpkeep = Triggers.upkeep(ActivePlayer.YOU);
        gameData.grantPerpetualAbility(cardId,
                new TriggeredAbility(new TriggerCondition(yourUpkeep, AbilityZones.EXILE), DRAW));

        assertThat(gameData.perpetualTriggerEffects(cardId, yourUpkeep, Zone.EXILE)).containsExactly(DRAW);
        assertThat(gameData.perpetualTriggerEffects(cardId, yourUpkeep, Zone.BATTLEFIELD)).isEmpty();
    }
}
