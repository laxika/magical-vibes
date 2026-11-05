package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.AttacksAlone;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSameNameAsSourcePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsGoadedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.service.trigger.CombatAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import com.github.laxika.magicalvibes.service.trigger.CombatWatch;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
        assertThatThrownBy(() -> CombatWatch.ON_ALLY_CREATURE_ATTACKS.effectsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> CombatWatch.ON_BLOCK.registrationsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void theReaderGivesTheFilterAndLimitFromTheAbilityAndWrapsTheInterveningIf() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureYouControlAttacks().withFilter(DRAGON),
                TriggerOptions.DEFAULT.withInterveningIf(ALONE).withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);

        List<CombatAbilityReader.CombatEffect> reads = CombatAbilityReader.read(
                WatchedAbilities.printed(card, CombatWatch.ON_ALLY_CREATURE_ATTACKS),
                Triggers.creatureYouControlAttacks());

        assertThat(reads).hasSize(1);
        CombatAbilityReader.CombatEffect read = reads.getFirst();
        assertThat(read.effect()).isEqualTo(new ConditionalEffect(ALONE, DRAW, true));
        assertThat(read.filter()).isEqualTo(DRAGON);
        assertThat(read.limit()).isEqualTo(TriggerLimit.ONCE_EACH_TURN);
        assertThat(read.ability()).isSameAs(card.getTriggeredAbilities().getFirst());
        assertThat(card.getTriggerEffects(Triggers.creatureYouControlAttacks(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void theEventConditionIsCheckedOnlyAsTheAbilityTriggersInsideTheInterveningIf() {
        Card card = new Card();
        card.addTrigger(Triggers.attacks(), TriggerOptions.DEFAULT.withEventCondition(ALONE)
                .withInterveningIf(ALONE), DRAW);

        assertThat(CombatAbilityReader.effects(WatchedAbilities.printed(card, CombatWatch.ON_ATTACK),
                Triggers.attacks())).containsExactly(
                new ConditionalEffect(ALONE, ConditionalEffect.atTriggerTime(ALONE, DRAW), true));
    }

    @Test
    void theAllyCombatDamageReadingTakesTheFilterAndOneOrMoreFromTheAbility() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), DRAW);
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(DRAGON),
                TriggerOptions.ONE_OR_MORE.withInterveningIf(ALONE), GAIN);
        CardEffect counter = new GainLifeEffect(3);
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(DRAGON),
                AllyCombatDamageTriggerEffect.boundToDealer(counter));
        Card twoFilters = new Card();
        twoFilters.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(DRAGON),
                new AllyCombatDamageTriggerEffect(DRAGON, DRAW));

        List<CombatAbilityReader.AllyCombatDamage> reads = CombatAbilityReader.read(
                        WatchedAbilities.printed(card, CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER),
                        Triggers.creatureYouControlDealsCombatDamageToPlayer()).stream()
                .map(CombatAbilityReader::allyCombatDamage).toList();

        assertThat(reads).containsExactly(
                new CombatAbilityReader.AllyCombatDamage(null, false, false, false, DRAW),
                new CombatAbilityReader.AllyCombatDamage(DRAGON, true, false, false,
                        new ConditionalEffect(ALONE, GAIN, true)),
                new CombatAbilityReader.AllyCombatDamage(DRAGON, false, true, false, counter));
        CombatAbilityReader.CombatEffect bothFilters = CombatAbilityReader.read(
                twoFilters.getTriggeredAbilities().getFirst(),
                Triggers.creatureYouControlDealsCombatDamageToPlayer()).getFirst();
        assertThatThrownBy(() -> CombatAbilityReader.allyCombatDamage(bothFilters))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anAuthoredAllyRecordKeepsItsIdentityAndTakesTheInterveningIfInside() {
        Card card = new Card();
        CardEffect authored = new AllyCombatDamageTriggerEffect(DRAGON, DRAW, true, true);
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), authored);
        Card guarded = new Card();
        guarded.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(ALONE), authored);

        assertThat(CombatAbilityReader.effects(WatchedAbilities.printed(card,
                        CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER),
                Triggers.creatureYouControlDealsCombatDamageToPlayer()).getFirst()).isSameAs(authored);
        assertThat(CombatAbilityReader.effects(WatchedAbilities.printed(guarded,
                        CombatWatch.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER),
                Triggers.creatureYouControlDealsCombatDamageToPlayer())).containsExactly(
                new AllyCombatDamageTriggerEffect(DRAGON, new ConditionalEffect(ALONE, DRAW, true), true, true));
    }

    @Test
    void theMultiplicityIsTheAbilitys() {
        Card card = new Card();
        card.addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, DRAW);
        card.addTrigger(Triggers.becomesBlocked().withFilter(DRAGON), TriggerOptions.EACH_BLOCKER, GAIN);
        CardEffect damaged = new DrawCardEffect(3);
        card.addTrigger(Triggers.creatureDealsCombatDamageToYou(), TriggerOptions.ONE_OR_MORE, damaged);

        CombatAbilityReader.CombatEffect blocks = CombatAbilityReader.read(
                WatchedAbilities.printed(card, CombatWatch.ON_BLOCK), Triggers.blocks()).getFirst();
        CombatAbilityReader.CombatEffect blocked = CombatAbilityReader.read(
                WatchedAbilities.printed(card, CombatWatch.ON_BECOMES_BLOCKED), Triggers.becomesBlocked()).getFirst();
        CombatAbilityReader.CombatEffect damage = CombatAbilityReader.read(
                WatchedAbilities.printed(card, CombatWatch.ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU),
                Triggers.creatureDealsCombatDamageToYou()).getFirst();

        assertThat(blocks.oneOrMore()).isTrue();
        assertThat(blocks.effect()).isSameAs(DRAW);
        assertThat(blocked.eachBlocker()).isTrue();
        assertThat(blocked.filter()).isEqualTo(DRAGON);
        assertThat(blocked.effect()).isSameAs(GAIN);
        assertThat(damage.oneOrMore()).isTrue();
        assertThat(damage.effect()).isSameAs(damaged);
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
    void aCollectorThatReadsNoFilterLimitOrMultiplicityRejectsOne() {
        Card filtered = new Card();
        filtered.addTrigger(Triggers.creatureYouControlAttacks().withFilter(DRAGON), DRAW);
        Card limited = new Card();
        limited.addTrigger(Triggers.attacks(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        Card oneOrMore = new Card();
        oneOrMore.addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, DRAW);

        assertThatThrownBy(() -> CombatAbilityReader.effects(
                WatchedAbilities.printed(filtered, CombatWatch.ON_ALLY_CREATURE_ATTACKS),
                Triggers.creatureYouControlAttacks())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CombatAbilityReader.effects(WatchedAbilities.printed(limited, CombatWatch.ON_ATTACK),
                Triggers.attacks())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CombatAbilityReader.effects(WatchedAbilities.printed(oneOrMore, CombatWatch.ON_BLOCK),
                Triggers.blocks())).isInstanceOf(IllegalStateException.class);
        assertThat(CombatAbilityReader.effects(WatchedAbilities.printed(filtered, CombatWatch.ON_ALLY_CREATURE_ATTACKS),
                Triggers.creatureYouControlAttacks(), DRAGON::equals)).containsExactly(DRAW);
        assertThat(CombatAbilityReader.effects(WatchedAbilities.printed(filtered, CombatWatch.ON_ALLY_CREATURE_ATTACKS),
                Triggers.creatureYouControlAttacks(), filter -> false)).isEmpty();
    }

    @Test
    void anAttacksOrBlocksAbilityIsReadByBothWatches() {
        Card card = new Card();
        card.addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), TriggerOptions.ONE_OR_MORE, DRAW);

        assertThat(card.getTriggeredAbilities()).hasSize(1);
        CombatAbilityReader.CombatEffect attack = CombatAbilityReader.read(
                WatchedAbilities.printed(card, CombatWatch.ON_ATTACK), Triggers.attacks()).getFirst();
        CombatAbilityReader.CombatEffect block = CombatAbilityReader.read(
                WatchedAbilities.printed(card, CombatWatch.ON_BLOCK), Triggers.blocks()).getFirst();
        assertThat(attack.ability()).isSameAs(block.ability());
        assertThat(attack.effect()).isSameAs(DRAW);
        assertThat(block.oneOrMore()).isTrue();
    }

    @Test
    void grantedAndRegisteredAbilitiesAreReadByTheReader() {
        TriggeredAbility ability = new TriggeredAbility(Triggers.creatureAttacks().withFilter(
                new PermanentIsTokenPredicate()), TriggerOptions.DEFAULT.withInterveningIf(ALONE), DRAW);
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(ability);

        assertThat(WatchedAbilities.temporary(permanent, Triggers.creatureAttacks())).containsExactly(ability);
        assertThat(WatchedAbilities.persistent(permanent, Triggers.creatureAttacks())).isEmpty();
        CombatAbilityReader.CombatEffect read = CombatAbilityReader.read(ability, Triggers.creatureAttacks())
                .getFirst();
        assertThat(read.filter()).isEqualTo(new PermanentIsTokenPredicate());
        assertThat(read.effect()).isEqualTo(new ConditionalEffect(ALONE, DRAW));
        assertThat(permanent.getTemporaryTriggerEffects(Triggers.creatureAttacks())).isEmpty();
        assertThatThrownBy(ability::ownEntryEffects).isInstanceOf(IllegalStateException.class);
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
    void theEffectsAreReadByAbilityAndKeepTheirSimultaneity() {
        Card card = new Card();
        card.addTrigger(Triggers.blocks(), DRAW, GAIN);
        card.addTrigger(Triggers.blocks(), DRAW);
        CardEffect draw = new DrawCardEffect(2);
        CardEffect life = new GainLifeEffect(2);
        card.addSimultaneousTrigger(Triggers.becomesBlocked(), draw, life);
        List<CombatAbilityReader.CombatEffect> blocks = CombatAbilityReader.read(
                WatchedAbilities.printed(card, CombatWatch.ON_BLOCK), Triggers.blocks());

        // Two equal abilities are still two (CR 603.3): the abilities are told apart by identity.
        assertThat(CombatAbilityReader.byAbility(blocks)).extracting(CombatAbilityReader::effectsOf)
                .containsExactly(List.of(DRAW, GAIN), List.of(DRAW));
        List<CardEffect> bundled = new ArrayList<>(List.of(DRAW, GAIN, DRAW));
        assertThat(CombatAbilityReader.splitLaterAbilities(blocks, bundled, effect -> effect))
                .containsExactly(List.of(DRAW));
        assertThat(bundled).containsExactly(GAIN, DRAW);
        assertThat(CombatAbilityReader.registrationsOf(CombatAbilityReader.read(
                WatchedAbilities.printed(card, CombatWatch.ON_BECOMES_BLOCKED), Triggers.becomesBlocked())))
                .containsExactly(new EffectRegistration(draw), new EffectRegistration(life, true));
        assertThat(card.isSimultaneousWithPrevious(life)).isTrue();
    }
}
