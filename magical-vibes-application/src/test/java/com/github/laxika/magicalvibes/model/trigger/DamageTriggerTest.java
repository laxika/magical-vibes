package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The combat-or-noncombat damage triggers: {@link DamageTrigger}. */
class DamageTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);

    @Test
    void functionsFromTheBattlefield() {
        assertThat(Triggers.dealsDamage().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.dealtDamage().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.youAreDealtDamage().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.sourceYouControlDealsNoncombatDamageToOpponent().defaultZones())
                .isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void namesTheEventAndWhoseObjectItIs() {
        assertThat(Triggers.dealsDamage())
                .isEqualTo(new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE, ObjectRelation.THIS))
                .isNotEqualTo(Triggers.enchantedCreatureDealsDamage())
                .isNotEqualTo(Triggers.sourceDealsDamage())
                .isNotEqualTo(Triggers.dealtDamage());
        assertThat(Triggers.dealtDamage())
                .isNotEqualTo(Triggers.enchantedCreatureDealtDamage())
                .isNotEqualTo(Triggers.permanentYouControlDealtDamage())
                .isNotEqualTo(Triggers.dealtNoncombatDamage())
                .isNotEqualTo(Triggers.creatureDealtDamage());
        assertThat(Triggers.youAreDealtDamage())
                .isNotEqualTo(Triggers.youAreDealtDamageBySourceYouControl())
                .isNotEqualTo(Triggers.youAreDealtDamageBySourceOpponentControls())
                .isNotEqualTo(Triggers.permanentDealsDamageToYou());
        assertThat(Triggers.dealsDamageToPlayer())
                .isNotEqualTo(Triggers.sourceYouControlDealsDamageToPlayer())
                .isNotEqualTo(Triggers.dealsDamageToOpponent());
    }

    @Test
    void isNotACombatTrigger() {
        assertThat(Triggers.dealsDamageToPlayer()).isNotEqualTo(Triggers.dealsCombatDamageToPlayer());
        assertThat(Triggers.dealtDamage()).isNotEqualTo(Triggers.dealtCombatDamage());
    }

    @Test
    void needsAnEventAndARelation() {
        assertThatThrownBy(() -> new DamageTrigger(null, ObjectRelation.THIS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new DamageTrigger(DamageTrigger.DamageEvent.DEALS_DAMAGE, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsDamageAbilitiesByTheZoneTheyFunctionFrom() {
        Card card = new Card();
        card.addTrigger(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(), DRAW);
        card.addTrigger(new TriggerCondition(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT, GAIN);

        assertThat(card.getTriggerEffects(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(), Zone.BATTLEFIELD))
                .containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.sourceYouControlDealsNoncombatDamageToOpponent(), Zone.GRAVEYARD))
                .containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.sourceYouControlDealsDamageToOpponent(), Zone.BATTLEFIELD))
                .isEmpty();
    }
}
