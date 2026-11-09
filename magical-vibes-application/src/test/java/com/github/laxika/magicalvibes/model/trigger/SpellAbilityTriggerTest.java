package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.filter.StackEntryIsCardExiledWithSourcePredicate;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemovePerpetualTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryKickedPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The spell, land-play and activation triggers: {@link SpellTrigger}, {@link LandPlayTrigger}, {@link AbilityTrigger}. */
class SpellAbilityTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);
    private static final CardEffect LOSE = new LoseLifeEffect(2);

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
    void anActivationStackEntryFilterNarrowsTheTriggerItsCollectorsReadItBy() {
        AbilityTrigger targetsSource = Triggers.youActivateAbility()
                .withStackEntryFilter(new StackEntryTargetsSourcePredicate());
        assertThat(targetsSource.stackEntry()).isEqualTo(new StackEntryTargetsSourcePredicate());
        assertThat(targetsSource).isNotEqualTo(Triggers.youActivateAbility());
        assertThat(targetsSource.unfiltered()).isEqualTo(Triggers.youActivateAbility());
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

    @Test
    void aFilteredLandPlayTriggerIsItsOwnWatchKeyAndNarrowsItsUnfilteredTrigger() {
        LandPlayTrigger island = Triggers.youPlayLand().withFilter(new CardSubtypePredicate(CardSubtype.ISLAND));
        LandPlayTrigger fromExile = Triggers.opponentPlaysLand().playedFrom(Zone.EXILE);

        assertThat(island).isNotEqualTo(Triggers.youPlayLand());
        assertThat(island.filtered()).isTrue();
        assertThat(island.watchKey()).isEqualTo(island);
        assertThat(island.unfiltered()).isEqualTo(Triggers.youPlayLand());
        assertThat(fromExile.filtered()).isTrue();
        assertThat(fromExile.watchKey()).isEqualTo(fromExile);
        assertThat(fromExile.unfiltered()).isEqualTo(Triggers.opponentPlaysLand());
        LandPlayTrigger unfiltered = Triggers.youPlayLand();
        assertThat(unfiltered.filtered()).isFalse();
        assertThat(unfiltered.unfiltered()).isSameAs(unfiltered);
        assertThatThrownBy(() -> Triggers.youPlayLand().withFilter(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Triggers.youPlayLand().playedFrom(Zone.EXILE).notPlayedFrom(Zone.HAND))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aCardExiledWithThisIsPlayedFromExileWhileExiledWithTheSource() {
        LandPlayTrigger land = Triggers.opponentPlaysLand().exiledWithThis();
        SpellTrigger spell = Triggers.playerCastsSpell().exiledWithThis();

        assertThat(land.exiledWithSource()).isTrue();
        assertThat(land.playedFrom()).isEqualTo(Zone.EXILE);
        assertThat(land.filtered()).isTrue();
        assertThat(land.unfiltered()).isEqualTo(Triggers.opponentPlaysLand());
        assertThat(spell.filter().castFrom()).isEqualTo(Zone.EXILE);
        assertThat(spell.filter().stackEntry()).isEqualTo(new StackEntryIsCardExiledWithSourcePredicate());
        assertThat(spell.unfiltered()).isEqualTo(Triggers.playerCastsSpell());
        assertThatThrownBy(() -> Triggers.youPlayLand().exiledWithThis().playedFrom(Zone.GRAVEYARD))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void landPlayAbilitiesHaveNoLegacyViewAndTheUnfilteredTriggerFindsOnlyItsOwnAbilities() {
        Card card = new Card();
        card.addTrigger(Triggers.youPlayLand().withFilter(new CardSubtypePredicate(CardSubtype.ISLAND)),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        card.addTrigger(Triggers.youPlayLand().notPlayedFrom(Zone.HAND), LOSE);
        card.addTrigger(Triggers.youPlayLand(), TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()),
                GAIN);

        // The land-play collectors read every land-play ability itself, its options included.
        assertThat(card.getTriggerEffects(Triggers.youPlayLand(), Zone.BATTLEFIELD)).containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.opponentPlaysLand(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void anActivationTriggerNarrowsItsUnfilteredTriggerByKindAndSource() {
        AbilityTrigger loyalty = Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.LOYALTY)
                .withSource(new PermanentIsCreaturePredicate());

        assertThat(loyalty.watchKey()).isEqualTo(loyalty);
        assertThat(loyalty.unfiltered()).isEqualTo(Triggers.youActivateAbility());
        AbilityTrigger unfiltered = Triggers.opponentActivatesAbility();
        assertThat(unfiltered.unfiltered()).isSameAs(unfiltered);
        assertThatThrownBy(() -> Triggers.youActivateAbility().withSource(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Triggers.youActivateAbility().withKind(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void anAbilityKindIsJudgedFromTheActivatedAbility() {
        ActivatedAbility tap = new ActivatedAbility(true, null, List.of(DRAW), "{T}: Draw a card.");
        ActivatedAbility mana = new ActivatedAbility(false, "{1}", List.of(DRAW), "{1}: Draw a card.");
        ActivatedAbility loyalty = new ActivatedAbility(1, List.of(DRAW), "+1: Draw a card.");

        assertThat(AbilityTrigger.AbilityKind.WITH_TAP_COST.matches(tap)).isTrue();
        assertThat(AbilityTrigger.AbilityKind.WITH_TAP_COST.matches(mana)).isFalse();
        assertThat(AbilityTrigger.AbilityKind.WITHOUT_TAP_COST.matches(mana)).isTrue();
        assertThat(AbilityTrigger.AbilityKind.WITHOUT_TAP_COST.matches(tap)).isFalse();
        assertThat(AbilityTrigger.AbilityKind.LOYALTY.matches(loyalty)).isTrue();
        assertThat(AbilityTrigger.AbilityKind.LOYALTY.matches(mana)).isFalse();
        assertThat(AbilityTrigger.AbilityKind.NINJUTSU.matches(tap)).isFalse();
        assertThat(AbilityTrigger.AbilityKind.BOAST.matches(tap)).isFalse();
    }

    @Test
    void anExhaustAbilityAndAnXInTheActivationCostAreKindsOfTheActivatedAbility() {
        ActivatedAbility exhaust = new ActivatedAbility(false, "{1}", List.of(DRAW), "Exhaust — {1}: Draw a card.")
                .withExhaust();
        ActivatedAbility xCost = new ActivatedAbility(false, "{X}{U}", List.of(DRAW), "{X}{U}: Draw a card.");
        ActivatedAbility plain = new ActivatedAbility(false, "{1}", List.of(DRAW), "{1}: Draw a card.");
        ActivatedAbility tap = new ActivatedAbility(true, null, List.of(DRAW), "{T}: Draw a card.");

        assertThat(AbilityTrigger.AbilityKind.EXHAUST.matches(exhaust)).isTrue();
        assertThat(AbilityTrigger.AbilityKind.EXHAUST.matches(plain)).isFalse();
        assertThat(AbilityTrigger.AbilityKind.WITH_X_IN_COST.matches(xCost)).isTrue();
        assertThat(AbilityTrigger.AbilityKind.WITH_X_IN_COST.matches(plain)).isFalse();
        assertThat(AbilityTrigger.AbilityKind.WITH_X_IN_COST.matches(tap)).isFalse();
    }

    @Test
    void aPaymentFilterNarrowsTheTriggerItsCollectorsReadItBy() {
        AbilityTrigger lifePaid = Triggers.youActivateNonManaAbility()
                .withPayment(AbilityTrigger.ActivationPayment.LIFE);
        assertThat(lifePaid.payment()).isEqualTo(AbilityTrigger.ActivationPayment.LIFE);
        assertThat(lifePaid).isNotEqualTo(Triggers.youActivateNonManaAbility())
                .isNotEqualTo(Triggers.youActivateNonManaAbility()
                        .withPayment(AbilityTrigger.ActivationPayment.SACRIFICE));
        assertThat(lifePaid.unfiltered()).isEqualTo(Triggers.youActivateNonManaAbility());
        assertThat(lifePaid.withKind(AbilityTrigger.AbilityKind.LOYALTY).payment())
                .isEqualTo(AbilityTrigger.ActivationPayment.LIFE);
        assertThatThrownBy(() -> Triggers.youActivateNonManaAbility().withPayment(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void anAbilityOfAPermanentIsActivatedByAnyPlayer() {
        AbilityTrigger anyPlayer = Triggers.playerActivatesNonManaAbility();
        assertThat(anyPlayer.player()).isEqualTo(PlayerRelation.ANY);
        assertThat(anyPlayer.event()).isEqualTo(AbilityTrigger.AbilityEvent.NONMANA_ACTIVATED);
        assertThat(anyPlayer).isNotEqualTo(Triggers.youActivateNonManaAbility())
                .isNotEqualTo(Triggers.opponentActivatesNonManaAbility());
        assertThat(anyPlayer.withSource(new PermanentIsCreaturePredicate()).unfiltered()).isEqualTo(anyPlayer);
    }

    @Test
    void aCastTriggerWithASpellFilterIsItsOwnWatchKeyAndNarrowsItsUnfilteredTrigger() {
        var creature = new CardTypePredicate(CardType.CREATURE);
        SpellTrigger creatureSpell = Triggers.youCastSpell().withFilter(creature).duringYourTurn().nthThisTurn(2);

        assertThat(creatureSpell.filter()).isEqualTo(SpellFilter.ANY.withSpell(creature)
                .withTurn(SpellFilter.Turn.YOURS).withNthThisTurn(2));
        assertThat(creatureSpell.watchKey()).isEqualTo(creatureSpell);
        assertThat(creatureSpell.unfiltered()).isEqualTo(Triggers.youCastSpell());
        assertThat(Triggers.opponentCastsSpell().anySpell()).isNotEqualTo(Triggers.opponentCastsSpell());
        assertThat(Triggers.opponentCastsSpell().anySpell().filter()).isEqualTo(SpellFilter.ANY);
        assertThat(Triggers.opponentCastsSpell().anySpell().unfiltered()).isEqualTo(Triggers.opponentCastsSpell());
        SpellTrigger unfiltered = Triggers.opponentCastsSpell();
        assertThat(unfiltered.unfiltered()).isSameAs(unfiltered);
        assertThatThrownBy(() -> Triggers.youCounterSpell().anySpell()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Triggers.youCastSpell().nthThisTurn(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.youCastSpell().withFilter(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void theUnfilteredCastTriggerFindsOnlyItsOwnAbilitiesAndNoneHasALegacyView() {
        var creature = new CardTypePredicate(CardType.CREATURE);
        Card card = new Card();
        card.addTrigger(Triggers.youCastSpell().withFilter(creature).duringOpponentsTurn().paidWithTreasureMana(),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceIsEnchantment()), DRAW, GAIN);
        card.addTrigger(Triggers.youCastSpell(), LOSE);
        TriggeredAbility granted = new TriggeredAbility(Triggers.youCastSpell().anySpell(), DRAW);

        // The unfiltered trigger finds only its own ability; no cast ability is in the legacy view.
        assertThat(card.getTriggerEffects(Triggers.youCastSpell(), Zone.BATTLEFIELD)).containsExactly(LOSE);
        assertThat(granted.hasWatchedTrigger(Triggers.youCastSpell())).isFalse();
        assertThat(granted.hasTrigger(Triggers.youCastSpell())).isFalse();
    }

    @Test
    void aCastTriggerNamesTheZoneTheSpellIsCastFromItsKickerAndWhoseSpellsItCounts() {
        assertThat(Triggers.youCastSpell().castFrom(Zone.GRAVEYARD).filter())
                .isEqualTo(SpellFilter.ANY.withCastFrom(Zone.GRAVEYARD));
        assertThat(Triggers.youCastSpell().notCastFrom(Zone.HAND).filter())
                .isEqualTo(SpellFilter.ANY.withNotCastFrom(Zone.HAND));
        assertThat(Triggers.playerCastsSpell().kicked().filter().kicker()).isEqualTo(SpellFilter.Kicker.KICKED);
        assertThat(Triggers.playerCastsSpell().eachKick().filter().kicker()).isEqualTo(SpellFilter.Kicker.EACH_KICK);
        assertThat(Triggers.playerCastsSpell().nthThisTurn(1).countingEveryPlayersSpells().filter().everyPlayersSpells())
                .isTrue();
        assertThat(Triggers.youCastSpell().kicked().unfiltered()).isEqualTo(Triggers.youCastSpell());
        assertThatThrownBy(() -> Triggers.youCastSpell().castFrom(Zone.EXILE).notCastFrom(Zone.HAND))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aCopyTriggerTakesTheFilterOfTheCopiedSpellAndItsStackObjectOnly() {
        var instant = new CardTypePredicate(CardType.INSTANT);
        Card card = new Card();
        card.addTrigger(Triggers.youCopySpell().withFilter(instant), DRAW, GAIN);
        card.addTrigger(Triggers.opponentCopiesSpell().withStackEntryFilter(new StackEntryKickedPredicate()), LOSE);

        assertThat(Triggers.youCopySpell().withFilter(instant).unfiltered()).isEqualTo(Triggers.youCopySpell());
        assertThat(Triggers.opponentCopiesSpell().withStackEntryFilter(new StackEntryKickedPredicate()).filter())
                .isEqualTo(SpellFilter.ANY.withStackEntry(new StackEntryKickedPredicate()));
        // The spell-copy collector reads a filtered copy ability itself, not through the legacy view.
        assertThat(card.getTriggerEffects(Triggers.youCopySpell(), Zone.BATTLEFIELD)).isEmpty();
        // A copy isn't cast: what the copied spell is is all a copy trigger can say.
        assertThatThrownBy(() -> Triggers.youCopySpell().nthThisTurn(1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.youCopySpell().castFrom(Zone.HAND))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
