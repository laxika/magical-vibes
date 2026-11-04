package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnPerCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PerDamageSourceTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.trigger.DamageWatch;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The combat-or-noncombat damage triggers: {@link DamageTrigger}. */
class DamageTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);
    private static final PermanentPredicate DRAGON = new PermanentHasSubtypePredicate(CardSubtype.DRAGON);

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
    void aFilterIsDroppedFromTheWatchKey() {
        assertThat(Triggers.permanentYouControlDealtDamage().withFilter(DRAGON).filter()).isEqualTo(DRAGON);
        assertThat(Triggers.permanentYouControlDealtDamage().withFilter(DRAGON).watchKey())
                .isEqualTo(Triggers.permanentYouControlDealtDamage());
        assertThat(Triggers.dealtDamage().watchKey()).isEqualTo(Triggers.dealtDamage());
        assertThatThrownBy(() -> Triggers.dealtDamage().withFilter(DRAGON).withFilter(DRAGON))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Triggers.dealtDamage().withFilter(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void theLegacyViewGivesTheFilterOutsideTheLimitAndTheLimitOutsideTheInterveningIf() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureDealtDamage().withFilter(DRAGON), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerTurn()).withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN), DRAW);

        List<CardEffect> legacy = DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE.effectsOf(card);

        assertThat(legacy).containsExactly(new TriggeringPermanentConditionalEffect(DRAGON,
                OncePerTurnTriggerEffect.markOnAcceptance(new ConditionalEffect(new ControllerTurn(), DRAW))));
        assertThat(DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE.effectsOf(card).getFirst()).isSameAs(legacy.getFirst());
        assertThat(card.getTriggerEffects(Triggers.creatureDealtDamage(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void theLegacyViewKeysTriggersOnlyOnceEachTurnByTheAbility() {
        Card card = new Card();
        card.addTrigger(Triggers.dealtDamage(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();

        assertThat(DamageWatch.ON_DEALT_DAMAGE.effectsOf(card))
                .containsExactly(OncePerTurnTriggerEffect.keyed(DRAW, ability.limitKey()));
    }

    @Test
    void eachSourceIsTheOutermostWrapperAndEachCreatureTheLimit() {
        Card card = new Card();
        card.addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE, DRAW);
        card.addTrigger(Triggers.permanentDealsDamageToYou(),
                TriggerOptions.EACH_SOURCE.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE), GAIN);

        assertThat(DamageWatch.ON_DEALT_DAMAGE.effectsOf(card)).containsExactly(new PerDamageSourceTriggerEffect(DRAW));
        assertThat(DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU.effectsOf(card)).containsExactly(
                new PerDamageSourceTriggerEffect(new OncePerTurnPerCreatureTriggerEffect(GAIN)));
    }

    @Test
    void aMultiplicityTheDamageCollectorsCantReadIsRejected() {
        Card oneOrMore = new Card();
        oneOrMore.addTrigger(Triggers.dealtDamage(), TriggerOptions.ONE_OR_MORE, DRAW);
        Card twoFilters = new Card();
        twoFilters.addTrigger(List.of(Triggers.dealtDamage().withFilter(DRAGON), Triggers.dealsDamage()), DRAW);

        assertThatThrownBy(() -> DamageWatch.ON_DEALT_DAMAGE.effectsOf(oneOrMore))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.attacks(), TriggerOptions.EACH_SOURCE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DamageWatch.ON_DEALT_DAMAGE.effectsOf(twoFilters))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anAbilityWithSeveralDamageConditionsIsReadByEachOfTheirWatches() {
        Card card = new Card();
        card.addTrigger(List.of(Triggers.sourceYouControlDealsDamageToPlayer().withFilter(DRAGON),
                Triggers.creatureYouControlDealsDamageToPlaneswalker().withFilter(DRAGON)), DRAW);

        assertThat(card.getTriggeredAbilities()).hasSize(1);
        assertThat(DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_PLAYER.effectsOf(card))
                .containsExactly(new TriggeringPermanentConditionalEffect(DRAGON, DRAW));
        assertThat(DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER.effectsOf(card).getFirst())
                .isSameAs(DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_PLAYER.effectsOf(card).getFirst());
    }

    @Test
    void grantedAbilitiesAreReadInTheLegacyFormToo() {
        TriggeredAbility ability = new TriggeredAbility(Triggers.dealsDamage().withFilter(DRAGON), DRAW);
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(ability);

        assertThat(permanent.getLegacyTemporaryTriggerEffects(Triggers.dealsDamage()))
                .containsExactly(new TriggeringPermanentConditionalEffect(DRAGON, DRAW));
        assertThat(permanent.getTemporaryTriggerEffects(Triggers.dealsDamage())).isEmpty();
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
