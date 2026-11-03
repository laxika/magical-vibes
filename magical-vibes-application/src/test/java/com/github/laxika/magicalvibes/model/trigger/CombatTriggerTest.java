package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BatchedCombatDamageToYouTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsGoadedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.service.trigger.CombatWatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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
    void theLegacyViewGivesTheFilterInsideTheLimitAndOutsideTheInterveningIf() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureYouControlAttacks().withFilter(DRAGON),
                TriggerOptions.DEFAULT.withInterveningIf(ALONE).withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();

        List<CardEffect> legacy = CombatWatch.ON_ALLY_CREATURE_ATTACKS.effectsOf(card);

        assertThat(legacy).containsExactly(OncePerTurnTriggerEffect.keyed(
                new TriggeringPermanentConditionalEffect(DRAGON, new ConditionalEffect(ALONE, DRAW)),
                ability.limitKey()));
        assertThat(CombatWatch.ON_ALLY_CREATURE_ATTACKS.effectsOf(card).getFirst()).isSameAs(legacy.getFirst());
        assertThat(card.getTriggerEffects(Triggers.creatureYouControlAttacks(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void aCreatureYouControlDealingCombatDamageToAPlayerIsReadAsTheAllyRecord() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), DRAW);
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(DRAGON),
                TriggerOptions.ONE_OR_MORE, new ConditionalEffect(ALONE, GAIN));
        CardEffect counter = new GainLifeEffect(3);
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(DRAGON),
                AllyCombatDamageTriggerEffect.boundToDealer(counter));

        assertThat(CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER.effectsOf(card)).containsExactly(
                new AllyCombatDamageTriggerEffect(null, DRAW),
                new AllyCombatDamageTriggerEffect(DRAGON, new ConditionalEffect(ALONE, GAIN), false, true),
                new AllyCombatDamageTriggerEffect(DRAGON, counter, true));
        assertThatThrownBy(() -> AllyCombatDamageTriggerEffect.boundToDealer(counter)
                .withTriggerOptions(DRAGON, false).withTriggerOptions(DRAGON, false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anAuthoredAllyRecordWithoutTriggerOptionsKeepsItsIdentity() {
        Card card = new Card();
        CardEffect authored = new AllyCombatDamageTriggerEffect(DRAGON, DRAW, true, true);
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), authored);

        assertThat(CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER.effectsOf(card).getFirst()).isSameAs(authored);
    }

    @Test
    void theMultiplicityIsTheRegistrationsTriggerModeOrTheBatchedWrapper() {
        Card card = new Card();
        card.addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, DRAW);
        card.addTrigger(Triggers.becomesBlocked().withFilter(DRAGON), TriggerOptions.EACH_BLOCKER, GAIN);
        CardEffect damaged = new DrawCardEffect(3);
        card.addTrigger(Triggers.creatureDealsCombatDamageToYou(), TriggerOptions.ONE_OR_MORE, damaged);

        assertThat(CombatWatch.ON_BLOCK.registrationsOf(card))
                .containsExactly(new EffectRegistration(DRAW, TriggerMode.ONCE_PER_BLOCK));
        assertThat(CombatWatch.ON_BECOMES_BLOCKED.registrationsOf(card)).containsExactly(new EffectRegistration(
                new TriggeringPermanentConditionalEffect(DRAGON, GAIN), TriggerMode.PER_BLOCKER));
        assertThat(CombatWatch.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU.effectsOf(card))
                .containsExactly(new BatchedCombatDamageToYouTriggerEffect(damaged));
    }

    @Test
    void aMultiplicityTheCollectorsCantReadIsRejected() {
        Card attacks = new Card();
        attacks.addTrigger(Triggers.attacks(), TriggerOptions.ONE_OR_MORE, DRAW);
        Card blocks = new Card();
        blocks.addTrigger(Triggers.blocks(), TriggerOptions.EACH_BLOCKER, DRAW);
        Card modes = new Card();
        modes.addTrigger(Triggers.becomesBlocked(), TriggerMode.PER_BLOCKER, TriggerOptions.EACH_BLOCKER, DRAW);

        assertThatThrownBy(() -> CombatWatch.ON_ATTACK.effectsOf(attacks)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CombatWatch.ON_BLOCK.effectsOf(blocks)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CombatWatch.ON_BECOMES_BLOCKED.effectsOf(modes))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anAttacksOrBlocksAbilityIsReadByBothWatches() {
        Card card = new Card();
        card.addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE, DRAW);

        assertThat(card.getTriggeredAbilities()).hasSize(1);
        assertThat(CombatWatch.ON_ATTACK.registrationsOf(card))
                .containsExactly(new EffectRegistration(DRAW, TriggerMode.ONCE_PER_BLOCK));
        assertThat(CombatWatch.ON_BLOCK.registrationsOf(card))
                .containsExactly(new EffectRegistration(DRAW, TriggerMode.ONCE_PER_BLOCK));
        assertThat(CombatWatch.ON_ATTACK.effectsOf(card).getFirst())
                .isSameAs(CombatWatch.ON_BLOCK.effectsOf(card).getFirst());
    }

    @Test
    void grantedAndRegisteredAbilitiesAreReadInTheLegacyFormToo() {
        TriggeredAbility ability = new TriggeredAbility(Triggers.creatureAttacks().withFilter(
                new PermanentIsTokenPredicate()), TriggerOptions.DEFAULT.withInterveningIf(ALONE), DRAW);
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(ability);

        assertThat(permanent.getLegacyTemporaryTriggerEffects(Triggers.creatureAttacks())).containsExactly(
                new TriggeringPermanentConditionalEffect(new PermanentIsTokenPredicate(),
                        new ConditionalEffect(ALONE, DRAW)));
        assertThat(permanent.getTemporaryTriggerEffects(Triggers.creatureAttacks())).isEmpty();
        assertThat(new TemporaryGlobalTriggeredAbility(UUID.randomUUID(), new Card(), ability).legacyEffect())
                .isEqualTo(new TriggeringPermanentConditionalEffect(new PermanentIsTokenPredicate(),
                        new ConditionalEffect(ALONE, DRAW)));
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
