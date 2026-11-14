package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsGoadedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The attack, block and combat damage triggers: {@link CombatTrigger}. */
class CombatTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);
    private static final PermanentPredicate DRAGON = new PermanentHasSubtypePredicate(CardSubtype.DRAGON);
    private static final Condition ALONE = new AttacksAlone();

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
    void aFilterIsDroppedFromTheWatchKeyButTheSameNameAndGoadedFiltersStay() {
        assertThat(Triggers.creatureYouControlAttacks().withFilter(DRAGON).filter()).isEqualTo(DRAGON);
        assertThat(Triggers.creatureYouControlAttacks().withFilter(DRAGON).watchKey())
                .isEqualTo(Triggers.creatureYouControlAttacks());
        assertThat(Triggers.creatureWithSameNameDealsCombatDamageToPlayer().watchKey())
                .isEqualTo(Triggers.creatureWithSameNameDealsCombatDamageToPlayer());
        assertThat(Triggers.goadedCreaturesDealCombatDamageToOpponent().watchKey())
                .isEqualTo(Triggers.goadedCreaturesDealCombatDamageToOpponent());
        assertThatThrownBy(() -> Triggers.creatureYouControlAttacks().withFilter(DRAGON).withFilter(DRAGON))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Triggers.attacks().withFilter(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCombatAbilityHasNoLegacyView() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureYouControlAttacks().withFilter(DRAGON), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(ability);

        assertThatThrownBy(ability::ownEntryEffects).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aMultiplicityTheCollectorsCantReadIsRejectedAsTheAbilityIsBuilt() {
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.attacks(), TriggerOptions.ONE_OR_MORE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.blocks(), TriggerOptions.EACH_BLOCKER, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.ONE_OR_MORE, DRAW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.attacks(), TriggerOptions.EACH_SOURCE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
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

    private static List<TriggeredAbility> printed(Card card, Trigger trigger) {
        return WatchedAbilities.printed(card, trigger, Zone.BATTLEFIELD);
    }
}
