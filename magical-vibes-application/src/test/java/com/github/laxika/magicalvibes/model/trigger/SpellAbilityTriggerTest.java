package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemovePerpetualTriggeredAbilityEffect;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The spell, land-play and activation triggers: {@link SpellTrigger}, {@link LandPlayTrigger}, {@link AbilityTrigger}. */
class SpellAbilityTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);

    @Test
    void anAbilityAboutThisSpellFunctionsFromTheStack() {
        assertThat(Triggers.youCastThisSpell().defaultZones()).isEqualTo(AbilityZones.STACK);
        assertThat(Triggers.thisSpellCounteredOrFizzles().defaultZones()).isEqualTo(AbilityZones.STACK);
    }

    @Test
    void anyOtherFunctionsFromTheBattlefield() {
        assertThat(Triggers.youCastSpell().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.opponentCopiesSpell().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.youCounterSpell().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.youPlayLand().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.opponentActivatesNonManaAbility().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void namesTheEventAndWhoActs() {
        assertThat(Triggers.youCastSpell())
                .isEqualTo(new SpellTrigger(SpellTrigger.SpellEvent.CAST, PlayerRelation.YOU))
                .isNotEqualTo(Triggers.opponentCastsSpell())
                .isNotEqualTo(Triggers.playerCastsSpell())
                .isNotEqualTo(Triggers.youCastThisSpell())
                .isNotEqualTo(Triggers.youCopySpell());
        assertThat(Triggers.youCounterSpell()).isNotEqualTo(Triggers.yourSpellCountered());
        assertThat(Triggers.youActivateAbility())
                .isEqualTo(new AbilityTrigger(AbilityTrigger.AbilityEvent.ACTIVATED, PlayerRelation.YOU))
                .isNotEqualTo(Triggers.youActivateNonManaAbility())
                .isNotEqualTo(Triggers.opponentActivatesAbility());
    }

    @Test
    void aLandPlayNamesHowTheLandRelatesToTheSource() {
        assertThat(Triggers.youPlayLand().relation()).isEqualTo(ObjectRelation.YOU_CONTROL);
        assertThat(Triggers.youPlayLand().relation().includesThis()).isTrue();
        assertThat(Triggers.youPlayAnotherLand().relation().includesThis()).isFalse();
        assertThat(Triggers.opponentPlaysLand().relation()).isEqualTo(ObjectRelation.OPPONENT_CONTROLS);
    }

    @Test
    void needsAnEventAndAPlayer() {
        assertThatThrownBy(() -> new SpellTrigger(null, PlayerRelation.YOU)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new SpellTrigger(SpellTrigger.SpellEvent.CAST, null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new AbilityTrigger(null, PlayerRelation.YOU)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LandPlayTrigger(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsCastAbilitiesByTheZoneTheyFunctionFrom() {
        Card card = new Card();
        card.addTrigger(Triggers.youCastSpell(), DRAW);
        card.addTrigger(new TriggerCondition(Triggers.youCastSpell(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                GAIN);
        card.addTrigger(Triggers.youCastThisSpell(), GAIN);

        assertThat(card.getTriggerEffects(Triggers.youCastSpell(), Zone.BATTLEFIELD)).containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.youCastSpell(), Zone.GRAVEYARD)).containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.youCastSpell(), Zone.HAND)).isEmpty();
        assertThat(card.getTriggerEffects(Triggers.youCastThisSpell(), Zone.STACK)).containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.youCastThisSpell(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void aPerpetualCastAbilityIsRemovedByItsTrigger() {
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID cardId = UUID.randomUUID();
        CardEffect loseLife = new LoseLifeEffect(1);
        gameData.grantPerpetualAbility(cardId, new TriggeredAbility(Triggers.youCastThisSpell(), loseLife));
        gameData.grantPerpetualAbility(cardId, new TriggeredAbility(Triggers.youCastSpell(), new LoseLifeEffect(2)));
        var removal = new RemovePerpetualTriggeredAbilityEffect(Triggers.youCastThisSpell(), LoseLifeEffect.class);

        gameData.removePerpetualAbilitiesIf(cardId, removal.triggeredAbilityTrigger(),
                removal.triggeredAbilityType()::isInstance);

        assertThat(gameData.perpetualTriggerEffects(cardId, Triggers.youCastThisSpell(), Zone.STACK)).isEmpty();
        assertThat(gameData.perpetualTriggerEffects(cardId, Triggers.youCastSpell(), Zone.BATTLEFIELD))
                .containsExactly(new LoseLifeEffect(2));
    }
}
