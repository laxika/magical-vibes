package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsGoadedPredicate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The attack, block and combat damage triggers: {@link CombatTrigger}. */
class CombatTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);

    @Test
    void functionsFromTheBattlefield() {
        assertThat(Triggers.attacks().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.youAttack().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.becomesBlocked().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.dealsCombatDamageToPlayer().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void namesTheEventAndWhoseCreatureItIs() {
        assertThat(Triggers.attacks())
                .isEqualTo(new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS, ObjectRelation.THIS))
                .isNotEqualTo(Triggers.creatureYouControlAttacks())
                .isNotEqualTo(Triggers.creatureAttacks())
                .isNotEqualTo(Triggers.youAttack())
                .isNotEqualTo(Triggers.attacksUnblocked());
        assertThat(Triggers.youAttack()).isNotEqualTo(Triggers.playerAttacks());
        assertThat(Triggers.dealsCombatDamageToPlayer())
                .isNotEqualTo(Triggers.equippedCreatureDealsCombatDamageToPlayer())
                .isNotEqualTo(Triggers.creatureYouControlDealsCombatDamageToPlayer())
                .isNotEqualTo(Triggers.dealsCombatDamageToPlayerOrPlaneswalker())
                .isNotEqualTo(Triggers.dealsCombatDamage());
        assertThat(Triggers.blocks()).isNotEqualTo(Triggers.creatureBlocks()).isNotEqualTo(Triggers.blocksTwoOrMore());
    }

    @Test
    void aFilterNarrowsTheCreature() {
        assertThat(Triggers.creatureDealsCombatDamageToPlayer().filter()).isNull();
        assertThat(Triggers.creatureWithSameNameDealsCombatDamageToPlayer())
                .isEqualTo(new CombatTrigger(CombatTrigger.CombatEvent.COMBAT_DAMAGE_TO_PLAYER, ObjectRelation.ANY,
                        new PermanentHasSameNameAsSourcePredicate()))
                .isNotEqualTo(Triggers.creatureDealsCombatDamageToPlayer());
        assertThat(Triggers.goadedCreaturesDealCombatDamageToOpponent().filter())
                .isEqualTo(new PermanentIsGoadedPredicate());
    }

    @Test
    void needsAnEventAndARelation() {
        assertThatThrownBy(() -> new CombatTrigger(null, ObjectRelation.THIS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CombatTrigger(CombatTrigger.CombatEvent.ATTACKS, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsAttackAbilitiesByTheZoneTheyFunctionFrom() {
        Card card = new Card();
        card.addTrigger(Triggers.youAttack(), DRAW);
        card.addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT, GAIN);

        assertThat(card.getTriggerEffects(Triggers.youAttack(), Zone.BATTLEFIELD)).containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.youAttack(), Zone.GRAVEYARD)).containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.youAttack(), Zone.EXILE)).isEmpty();
        assertThat(card.getTriggerEffects(Triggers.attacks(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void aRegistrationKeepsItsTriggerMode() {
        Card card = new Card();
        card.addTrigger(Triggers.blocks(), DRAW);
        card.addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK, GAIN);
        CardEffect draw = new DrawCardEffect(2);
        CardEffect life = new GainLifeEffect(2);
        card.addSimultaneousTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, draw, life);

        assertThat(card.getTriggerEffectRegistrations(Triggers.blocks(), Zone.BATTLEFIELD)).containsExactly(
                new EffectRegistration(DRAW), new EffectRegistration(GAIN, TriggerMode.ONCE_PER_BLOCK));
        assertThat(card.getTriggerEffectRegistrations(Triggers.becomesBlocked(), Zone.BATTLEFIELD)).containsExactly(
                new EffectRegistration(draw, TriggerMode.PER_BLOCKER),
                new EffectRegistration(life, TriggerMode.PER_BLOCKER, true));
        assertThat(card.isSimultaneousWithPrevious(life)).isTrue();
    }
}
