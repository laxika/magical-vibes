package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerChosenBySourcePredicate;
import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import org.junit.jupiter.api.Test;


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
    }

    @Test
    void aMultiplicityNoDamageCollectorReadsIsRejectedAsTheAbilityIsBuilt() {
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.dealtDamage(), TriggerOptions.ONE_OR_MORE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.attacks(), TriggerOptions.EACH_SOURCE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
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
        assertThat(damageCondition(card.getTriggeredAbilities().getFirst(),
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
        assertThat(WatchedAbilities.printed(card, Triggers.sourceDealsDamage(), Zone.BATTLEFIELD)).hasSize(1);
        assertThat(damageCondition(card.getTriggeredAbilities().getFirst(),
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
        assertThat(WatchedAbilities.printed(card, Triggers.sourceDealsDamageToYou(), Zone.BATTLEFIELD)).hasSize(1);
        assertThat(WatchedAbilities.printed(card, Triggers.sourceDealsDamage(), Zone.BATTLEFIELD)).isEmpty();
        assertThat(damageCondition(card.getTriggeredAbilities().getFirst(),
                Triggers.sourceDealsDamageToYou())).isEqualTo(circle);
    }

    @Test
    void anInstantOrSorceryDealsDamageToItsOnlyTargetCreatureTriggerHasItsOwnWatch() {
        Card card = new Card();
        card.addTrigger(Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature(), DRAW);

        assertThat(Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature())
                .isNotEqualTo(Triggers.instantOrSorceryYouControlDealsDamage());
        assertThat(WatchedAbilities.printed(card, Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature(), Zone.BATTLEFIELD)).hasSize(1);
        assertThat(WatchedAbilities.printed(card, Triggers.instantOrSorceryYouControlDealsDamage(), Zone.BATTLEFIELD)).isEmpty();
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
    void aDealsDamageToAPermanentOrPlayerTriggerFiltersTheRecipientAndHasItsOwnWatches() {
        PlayerChosenBySourcePredicate chosen = new PlayerChosenBySourcePredicate();
        DamageTrigger filtered = Triggers.sourceDealsDamageToPermanentOrPlayer().withDamagedPlayerFilter(chosen)
                .withDamagedFilter(DRAGON);

        assertThat(filtered.damagedPlayerFilter()).isEqualTo(chosen);
        assertThat(filtered.damagedFilter()).isEqualTo(DRAGON);
        assertThat(filtered.watchKey()).isEqualTo(Triggers.sourceDealsDamageToPermanentOrPlayer());
        assertThat(Triggers.anotherSourceYouControlDealsDamageToPermanentOrPlayer())
                .isNotEqualTo(Triggers.sourceDealsDamageToPermanentOrPlayer());

        Card card = new Card();
        card.addTrigger(filtered, DRAW);
        card.addTrigger(Triggers.anotherSourceYouControlDealsDamageToPermanentOrPlayer(), DRAW);
        assertThat(WatchedAbilities.printed(card, Triggers.sourceDealsDamageToPermanentOrPlayer(), Zone.BATTLEFIELD))
                .hasSize(1);
        assertThat(WatchedAbilities.printed(card, Triggers.anotherSourceYouControlDealsDamageToPermanentOrPlayer(), Zone.BATTLEFIELD)).hasSize(1);
        assertThat(WatchedAbilities.printed(card, Triggers.sourceDealsDamage(), Zone.BATTLEFIELD)).isEmpty();
        assertThatThrownBy(() -> Triggers.sourceDealsDamage().withDamagedPlayerFilter(chosen))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.sourceDealsDamageToPermanentOrPlayer().withCombatDamageOnly())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aPlayerDealtDamageTriggerFiltersThePlayerAndHasItsOwnWatch() {
        PlayerChosenBySourcePredicate chosen = new PlayerChosenBySourcePredicate();
        DamageTrigger sower = Triggers.playerDealtDamage().withDamagedPlayerFilter(chosen);

        assertThat(sower.damagedPlayerFilter()).isEqualTo(chosen);
        assertThat(sower.watchKey()).isEqualTo(Triggers.playerDealtDamage());
        assertThat(Triggers.playerDealtDamage()).isNotEqualTo(Triggers.sourceDealsDamageToPermanentOrPlayer());

        Card card = new Card();
        card.addTrigger(sower, DRAW);
        assertThat(WatchedAbilities.printed(card, Triggers.playerDealtDamage(), Zone.BATTLEFIELD)).hasSize(1);
        assertThat(WatchedAbilities.printed(card, Triggers.sourceDealsDamageToPermanentOrPlayer(), Zone.BATTLEFIELD))
                .isEmpty();
        assertThatThrownBy(() -> Triggers.playerDealtDamage().withFilter(DRAGON))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.youAreDealtDamage().withDamagedPlayerFilter(chosen))
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

    /** {@code ability}'s damage condition watched as {@code watched}, with its filters, or {@code null}. */
    private static DamageTrigger damageCondition(TriggeredAbility ability, Trigger watched) {
        return ability.conditions().stream().map(TriggerCondition::trigger)
                .filter(trigger -> trigger instanceof DamageTrigger damage && damage.watchKey().equals(watched))
                .map(DamageTrigger.class::cast).findFirst().orElse(null);
    }
}
