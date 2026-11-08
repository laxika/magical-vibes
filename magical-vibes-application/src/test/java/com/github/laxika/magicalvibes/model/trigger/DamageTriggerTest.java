package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.trigger.DamageAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.DamageWatch;
import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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
    void aDamageAbilityHasNoLegacyView() {
        Card card = new Card();
        card.addTrigger(Triggers.dealtDamage(), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(ability);

        assertThatThrownBy(ability::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> DamageWatch.ON_DEALT_DAMAGE.effectsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> DamageWatch.ON_DEALT_DAMAGE.registrationsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void theReaderGivesTheFilterAndTheLimitAndWrapsTheInterveningIfAroundTheEffect() {
        Card card = new Card();
        card.addTrigger(Triggers.creatureDealtDamage().withFilter(DRAGON), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerTurn()).withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN), DRAW);

        List<DamageAbilityReader.DamageEffect> reads = DamageAbilityReader.read(
                DamageAbilityReader.printed(card, DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE),
                Triggers.creatureDealtDamage());

        assertThat(reads).singleElement().satisfies(read -> {
            assertThat(read.effect()).isEqualTo(new ConditionalEffect(new ControllerTurn(), DRAW));
            assertThat(read.filter()).isEqualTo(DRAGON);
            assertThat(read.limit()).isEqualTo(TriggerLimit.DO_ONLY_ONCE_EACH_TURN);
            assertThat(read.marksLimitOnAcceptance()).isTrue();
            assertThat(read.eachSource()).isFalse();
        });
    }

    @Test
    void triggersOnlyOnceEachTurnIsMarkedByTheAbilityOnItsSource() {
        Card card = new Card();
        card.addTrigger(Triggers.dealtDamage(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        card.addTrigger(Triggers.dealtDamage(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), GAIN);
        List<DamageAbilityReader.DamageEffect> reads = DamageAbilityReader.read(
                DamageAbilityReader.printed(card, DamageWatch.ON_DEALT_DAMAGE), Triggers.dealtDamage());
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        reads.getFirst().markLimit(gameData, sourceId);

        assertThat(reads.getFirst().limitReached(gameData, sourceId)).isTrue();
        assertThat(reads.getFirst().limitReached(gameData, UUID.randomUUID())).isFalse();
        assertThat(reads.get(1).limitReached(gameData, sourceId)).isFalse();
    }

    @Test
    void eachSourceIsTheMultiplicityAndEachCreatureTheLimitForTheCreatureTheEventNames() {
        Card card = new Card();
        card.addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE, DRAW);
        card.addTrigger(Triggers.permanentDealsDamageToYou(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE), GAIN);
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();
        UUID creatureId = UUID.randomUUID();

        DamageAbilityReader.DamageEffect eachSource = DamageAbilityReader.read(
                DamageAbilityReader.printed(card, DamageWatch.ON_DEALT_DAMAGE), Triggers.dealtDamage()).getFirst();
        DamageAbilityReader.DamageEffect eachCreature = DamageAbilityReader.read(
                DamageAbilityReader.printed(card, DamageWatch.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU),
                Triggers.permanentDealsDamageToYou()).getFirst();
        eachCreature.markLimit(gameData, sourceId, creatureId);

        assertThat(eachSource.eachSource()).isTrue();
        assertThat(eachSource.effect()).isEqualTo(DRAW);
        assertThat(eachCreature.limitReached(gameData, sourceId, creatureId)).isTrue();
        assertThat(eachCreature.limitReached(gameData, sourceId, UUID.randomUUID())).isFalse();
    }

    @Test
    void aMultiplicityNoDamageCollectorReadsIsRejectedAsTheAbilityIsBuilt() {
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.dealtDamage(), TriggerOptions.ONE_OR_MORE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.attacks(), TriggerOptions.EACH_SOURCE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theStrictReaderRejectsAFilterALimitOrAMultiplicity() {
        Card filtered = new Card();
        filtered.addTrigger(Triggers.dealtDamage().withFilter(DRAGON), DRAW);
        Card limited = new Card();
        limited.addTrigger(Triggers.dealtDamage(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE), DRAW);
        Card eachSource = new Card();
        eachSource.addTrigger(Triggers.dealtDamage(), TriggerOptions.EACH_SOURCE, DRAW);
        Card plain = new Card();
        plain.addTrigger(Triggers.dealtDamage(), DRAW);

        assertThatThrownBy(() -> DamageAbilityReader.printedEffects(filtered, DamageWatch.ON_DEALT_DAMAGE))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> DamageAbilityReader.printedEffects(limited, DamageWatch.ON_DEALT_DAMAGE))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> DamageAbilityReader.printedEffects(eachSource, DamageWatch.ON_DEALT_DAMAGE))
                .isInstanceOf(IllegalStateException.class);
        assertThat(DamageAbilityReader.printedEffects(plain, DamageWatch.ON_DEALT_DAMAGE)).containsExactly(DRAW);
        assertThat(DamageAbilityReader.effects(DamageAbilityReader.printed(filtered, DamageWatch.ON_DEALT_DAMAGE),
                Triggers.dealtDamage(), filter -> false)).isEmpty();
        assertThat(DamageAbilityReader.effects(DamageAbilityReader.printed(filtered, DamageWatch.ON_DEALT_DAMAGE),
                Triggers.dealtDamage(), DRAGON::equals)).containsExactly(DRAW);
    }

    @Test
    void anAbilityWithSeveralDamageConditionsIsReadByEachOfTheirWatchesWithItsOwnFilter() {
        PermanentPredicate pirate = new PermanentHasSubtypePredicate(CardSubtype.PIRATE);
        Card card = new Card();
        card.addTrigger(List.of(Triggers.sourceYouControlDealsDamageToPlayer().withFilter(DRAGON),
                Triggers.creatureYouControlDealsDamageToPlaneswalker().withFilter(pirate)), DRAW);

        assertThat(card.getTriggeredAbilities()).hasSize(1);
        assertThat(DamageAbilityReader.read(
                DamageAbilityReader.printed(card, DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_PLAYER),
                Triggers.sourceYouControlDealsDamageToPlayer()))
                .singleElement().extracting(DamageAbilityReader.DamageEffect::filter).isEqualTo(DRAGON);
        assertThat(DamageAbilityReader.read(
                DamageAbilityReader.printed(card, DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER),
                Triggers.creatureYouControlDealsDamageToPlaneswalker()))
                .singleElement().extracting(DamageAbilityReader.DamageEffect::filter).isEqualTo(pirate);
    }

    @Test
    void grantedAbilitiesAreFoundByTheWatchedTriggerAndReadWithTheirFilter() {
        TriggeredAbility ability = new TriggeredAbility(Triggers.dealsDamage().withFilter(DRAGON), DRAW);
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(ability);

        assertThat(WatchedAbilities.temporary(permanent, Triggers.dealsDamage())).containsExactly(ability);
        assertThat(DamageAbilityReader.read(WatchedAbilities.temporary(permanent, Triggers.dealsDamage()),
                Triggers.dealsDamage())).singleElement()
                .extracting(DamageAbilityReader.DamageEffect::filter).isEqualTo(DRAGON);
        assertThat(permanent.getTemporaryTriggerEffects(Triggers.dealsDamage())).isEmpty();
    }

    @Test
    void aDealsDamageToACreatureTriggerNamesItsDealerTheDamagedCreatureAndCombatDamage() {
        DamageTrigger vampireSlayer = Triggers.dealsDamageToCreature().withDamagedFilter(DRAGON);
        DamageTrigger sosuke = Triggers.creatureYouControlDealsDamageToCreature().withFilter(DRAGON)
                .withCombatDamageOnly();

        assertThat(vampireSlayer.damagedFilter()).isEqualTo(DRAGON);
        assertThat(vampireSlayer.filter()).isNull();
        assertThat(vampireSlayer.combatOnly()).isFalse();
        assertThat(sosuke.filter()).isEqualTo(DRAGON);
        assertThat(sosuke.combatOnly()).isTrue();
        assertThat(vampireSlayer.watchKey()).isEqualTo(Triggers.dealsDamageToCreature());
        assertThat(sosuke.watchKey()).isEqualTo(Triggers.creatureYouControlDealsDamageToCreature());
        assertThat(Triggers.dealsDamageToCreature())
                .isNotEqualTo(Triggers.creatureYouControlDealsDamageToCreature())
                .isNotEqualTo(Triggers.equippedCreatureDealsDamageToCreature())
                .isNotEqualTo(Triggers.creatureDealsDamageToCreature());

        Card card = new Card();
        card.addTrigger(sosuke, DRAW);
        assertThat(DamageAbilityReader.trigger(card.getTriggeredAbilities().getFirst(),
                Triggers.creatureYouControlDealsDamageToCreature())).isEqualTo(sosuke);
    }

    @Test
    void onlyADealsDamageToACreatureTriggerHasADamagedCreatureFilterOrIsCombatOnly() {
        assertThatThrownBy(() -> Triggers.dealsDamage().withDamagedFilter(DRAGON))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.dealtDamage().withCombatDamageOnly())
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.dealsDamageToCreature().withDamagedFilter(DRAGON).withDamagedFilter(DRAGON))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aSourceDealsDamageTriggerNamesTheSourceAsAPermanentAndAsASpell() {
        CardPredicate redSpell = new CardColorPredicate(CardColor.RED);
        DamageTrigger justice = Triggers.sourceDealsDamage().withFilter(DRAGON).withSpellFilter(redSpell);
        DamageTrigger redTerror = Triggers.sourceYouControlDealsDamage().withSpellFilter(redSpell);

        assertThat(justice.filter()).isEqualTo(DRAGON);
        assertThat(justice.spellFilter()).isEqualTo(redSpell);
        assertThat(justice.watchKey()).isEqualTo(Triggers.sourceDealsDamage());
        assertThat(redTerror.filter()).isNull();
        assertThat(redTerror.watchKey()).isEqualTo(Triggers.sourceYouControlDealsDamage());
        assertThat(Triggers.sourceYouControlDealsDamage()).isNotEqualTo(Triggers.sourceDealsDamage());

        Card card = new Card();
        card.addTrigger(justice, DRAW);
        assertThat(DamageAbilityReader.printed(card, DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE)).hasSize(1);
        assertThat(DamageAbilityReader.trigger(card.getTriggeredAbilities().getFirst(),
                Triggers.sourceDealsDamage())).isEqualTo(justice);
    }

    @Test
    void onlyATriggerOnAnotherSourceDealingDamageHasASpellFilter() {
        CardPredicate redSpell = new CardColorPredicate(CardColor.RED);
        assertThatThrownBy(() -> Triggers.dealsDamage().withSpellFilter(redSpell))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.permanentDealsDamageToYou().withSpellFilter(redSpell))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.sourceDealsDamage().withSpellFilter(redSpell).withSpellFilter(redSpell))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aSourceDealsDamageToYouTriggerNamesTheSourceAsAPermanentAndAsASpell() {
        CardPredicate redSpell = new CardColorPredicate(CardColor.RED);
        DamageTrigger circle = Triggers.sourceDealsDamageToYou().withFilter(DRAGON).withSpellFilter(redSpell);

        assertThat(circle.filter()).isEqualTo(DRAGON);
        assertThat(circle.spellFilter()).isEqualTo(redSpell);
        assertThat(circle.watchKey()).isEqualTo(Triggers.sourceDealsDamageToYou());
        assertThat(Triggers.sourceDealsDamageToYou()).isNotEqualTo(Triggers.sourceDealsDamage())
                .isNotEqualTo(Triggers.permanentDealsDamageToYou());

        Card card = new Card();
        card.addTrigger(circle, DRAW);
        assertThat(DamageAbilityReader.printed(card, DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE_TO_YOU)).hasSize(1);
        assertThat(DamageAbilityReader.printed(card, DamageWatch.ON_ANY_SOURCE_DEALS_DAMAGE)).isEmpty();
        assertThat(DamageAbilityReader.trigger(card.getTriggeredAbilities().getFirst(),
                Triggers.sourceDealsDamageToYou())).isEqualTo(circle);
    }

    @Test
    void anInstantOrSorceryDealsDamageToItsOnlyTargetCreatureTriggerHasItsOwnWatch() {
        Card card = new Card();
        card.addTrigger(Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature(), DRAW);

        assertThat(Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature())
                .isNotEqualTo(Triggers.instantOrSorceryYouControlDealsDamage());
        assertThat(DamageAbilityReader.printed(card,
                DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_ITS_ONLY_TARGET_CREATURE)).hasSize(1);
        assertThat(DamageAbilityReader.printed(card, DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE)).isEmpty();
        assertThatThrownBy(() -> Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature()
                .withSpellFilter(new CardColorPredicate(CardColor.RED)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aCreatureDealsDamageToYouOrYourPermanentTriggerTakesADamagedPermanentFilter() {
        DamageTrigger mangara = Triggers.creatureDealsDamageToYouOrYourPermanent().withFilter(DRAGON)
                .withDamagedFilter(DRAGON);

        assertThat(mangara.damagedFilter()).isEqualTo(DRAGON);
        assertThat(mangara.watchKey()).isEqualTo(Triggers.creatureDealsDamageToYouOrYourPermanent());
        assertThatThrownBy(() -> Triggers.creatureDealsDamageToYouOrYourPermanent().withCombatDamageOnly())
                .isInstanceOf(IllegalArgumentException.class);
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
