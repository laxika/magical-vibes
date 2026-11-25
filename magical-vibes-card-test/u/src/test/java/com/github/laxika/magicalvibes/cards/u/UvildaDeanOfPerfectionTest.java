package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MultipleChoice;
import com.github.laxika.magicalvibes.cards.m.MatterReshaper;
import com.github.laxika.magicalvibes.cards.n.NassariDeanOfExpression;
import com.github.laxika.magicalvibes.cards.s.SerpentineCurve;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UvildaDeanOfPerfection.class, NassariDeanOfExpression.class, DarkRitual.class,
        GrizzlyBears.class, SerpentineCurve.class, MultipleChoice.class, MatterReshaper.class})
class UvildaDeanOfPerfectionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an instant from hand with three refine counters")
    void exilesAnInstantFromHandWithThreeRefineCounters() {
        addReadyUvilda();
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(new GrizzlyBears(), ritual));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
        assertThat(gd.exiledCardsWithCounter(CounterType.REFINE)).containsEntry(ritual.getId(), 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("Last refine counter offers the spell for four less and casts it")
    void lastRefineCounterOffersTheSpellForFourLessAndCastsIt() {
        addReadyUvilda();
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player1, List.of(ritual));
        gd.setExiledCardCounters(ritual.getId(), CounterType.REFINE, 1);

        offerLastCounterCast();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThat(gd.exiledCardsWithCounter(CounterType.REFINE)).doesNotContainKey(ritual.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(ritual.getId())).isNull();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(ritual.getId()));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.assertInGraveyard(player1, "Dark Ritual");
    }

    @Test
    @DisplayName("Nassari exiles each opponents top spell and counts the cast")
    void nassariExilesEachOpponentsTopSpellAndCountsTheCast() {
        UvildaDeanOfPerfection card = new UvildaDeanOfPerfection();
        Permanent nassari = harness.addToBattlefieldAndReturn(player1, card);
        nassari.setCard(card.getBackFaceCard());
        nassari.setTransformed(true);
        nassari.setSummoningSick(false);
        harness.setLibrary(player2, List.of(new DarkRitual()));

        triggerUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c instanceof DarkRitual)
                .findFirst()
                .orElseThrow();
        assertThat(gd.exilePlayPermissionHolders()).containsEntry(exiled.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaTypeCardIds()).contains(exiled.getId());

        harness.castFromExile(player1, exiled.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(nassari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining to exile still pays the tap cost")
    void decliningToExileStillPaysTheTapCost() {
        Permanent uvilda = addReadyUvilda();
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(ritual));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(uvilda.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ritual);
        assertThat(gd.findExiledCard(ritual.getId())).isNull();
    }

    @Test
    @DisplayName("Cannot choose a creature instead of an instant or sorcery")
    void cannotChooseACreatureInsteadOfAnInstantOrSorcery() {
        addReadyUvilda();
        GrizzlyBears bears = new GrizzlyBears();
        SerpentineCurve curve = new SerpentineCurve();
        harness.setHand(player1, List.of(bears, curve));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears, curve);
        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);

        harness.handleCardChosen(player1, 1);
        assertThat(gd.findExiledCard(curve.getId())).isNotNull();
        assertThat(gd.exiledCardsWithCounter(CounterType.REFINE)).containsEntry(curve.getId(), 3);
    }

    @Test
    @DisplayName("Countdown continues without Uvilda and only on the owners upkeep")
    void countdownContinuesWithoutUvildaAndOnlyOnTheOwnersUpkeep() {
        addReadyUvilda();
        SerpentineCurve curve = new SerpentineCurve();
        harness.setHand(player1, List.of(curve));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        gd.playerBattlefields.get(player1.getId()).clear();

        triggerUpkeep(player2);
        assertThat(gd.exiledCardsWithCounter(CounterType.REFINE)).containsEntry(curve.getId(), 3);

        triggerUpkeep(player1);
        assertThat(gd.exiledCardsWithCounter(CounterType.REFINE)).containsEntry(curve.getId(), 2);
        assertThat(gd.findExiledCard(curve.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Last counter removal creates a separate respondable cast trigger")
    void lastCounterRemovalCreatesASeparateRespondableCastTrigger() {
        SerpentineCurve curve = new SerpentineCurve();
        harness.setExile(player1, List.of(curve));
        gd.setExiledCardCounters(curve.getId(), CounterType.REFINE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UNTAP);
            harness.clearPriorityPassed();
            harness.passUntil(TurnStep.UPKEEP);
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();

            assertThat(gd.exiledCardsWithCounter(CounterType.REFINE)).doesNotContainKey(curve.getId());
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.stack).singleElement().satisfies(entry -> {
                assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
                assertThat(entry.getCard().getId()).isEqualTo(curve.getId());
            });
        });
    }

    @Test
    @DisplayName("Casts a sorcery during upkeep paying only its colored cost")
    void castsASorceryDuringUpkeepPayingOnlyItsColoredCost() {
        SerpentineCurve curve = new SerpentineCurve();
        prepareLastCounterCast(curve);

        offerLastCounterCast();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Serpentine Curve");
        harness.assertOnBattlefield(player1, "Fractal");
    }

    @Test
    @DisplayName("Declining the last counter cast does not grant a later cast permission")
    void decliningTheLastCounterCastDoesNotGrantALaterCastPermission() {
        SerpentineCurve curve = new SerpentineCurve();
        prepareLastCounterCast(curve);
        harness.addMana(player1, ManaColor.BLUE, 1);

        offerLastCounterCast();
        harness.handleMayAbilityChosen(player1, false);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThat(gd.findExiledCard(curve.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, curve.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Reduced cost cast allows choosing a nonzero X")
    void reducedCostCastAllowsChoosingANonzeroX() {
        MultipleChoice choice = new MultipleChoice();
        prepareLastCounterCast(choice);

        offerLastCounterCast();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);
        harness.handleXValueChosen(player1, 3);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.assertOnBattlefield(player1, "Elemental");
        harness.assertInGraveyard(player1, "Multiple Choice");
    }

    @Test
    @DisplayName("Nassari sorcery permission uses normal timing and any color of mana")
    void nassariSorceryPermissionUsesNormalTimingAndAnyColorOfMana() {
        Permanent nassari = harness.addToBattlefieldAndReturn(player1, new NassariDeanOfExpression());
        SerpentineCurve curve = new SerpentineCurve();
        harness.setLibrary(player2, List.of(curve));
        harness.addMana(player1, ManaColor.RED, 4);

        triggerUpkeep(player1);
        assertThatThrownBy(() -> harness.castFromExile(player1, curve.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castFromExile(player1, curve.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(nassari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Serpentine Curve");
        harness.assertOnBattlefield(player1, "Fractal");
    }

    @Test
    @DisplayName("Nassari counts a spell exiled by Uvilda")
    void nassariCountsASpellExiledByUvilda() {
        Permanent nassari = harness.addToBattlefieldAndReturn(player1, new NassariDeanOfExpression());
        SerpentineCurve curve = new SerpentineCurve();
        harness.setLibrary(player2, List.of());
        prepareLastCounterCast(curve);

        offerLastCounterCast();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(nassari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Serpentine Curve");
    }

    @Test
    @DisplayName("Reduced cost does not eliminate colored mana requirements")
    void reducedCostDoesNotEliminateColoredManaRequirements() {
        SerpentineCurve curve = new SerpentineCurve();
        prepareLastCounterCast(curve);
        harness.addMana(player1, ManaColor.RED, 4);

        offerLastCounterCast();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(curve.getId())).isNotNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(curve.getId()));
        harness.assertNotOnBattlefield(player1, "Fractal");
    }

    @Test
    @DisplayName("Nassari cannot use colored mana to pay an explicit colorless requirement")
    void nassariCannotUseColoredManaToPayAnExplicitColorlessRequirement() {
        harness.addToBattlefield(player1, new NassariDeanOfExpression());
        MatterReshaper reshaper = new MatterReshaper();
        harness.setLibrary(player2, List.of(reshaper));

        triggerUpkeep(player1);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, reshaper.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(reshaper.getId())).isNotNull();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, reshaper.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.assertOnBattlefield(player1, "Matter Reshaper");
    }

    @Test
    @DisplayName("Nassari does not count a spell cast from hand")
    void nassariDoesNotCountASpellCastFromHand() {
        Permanent nassari = harness.addToBattlefieldAndReturn(player1, new NassariDeanOfExpression());
        SerpentineCurve curve = new SerpentineCurve();
        harness.setHand(player1, List.of(curve));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, curve, "{3}{U}");
        harness.passBothPriorities();

        assertThat(nassari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Serpentine Curve");
    }

    @Test
    @DisplayName("Nassari permission expires after the turn even though the card remains exiled")
    void nassariPermissionExpiresAfterTheTurnEvenThoughTheCardRemainsExiled() {
        harness.addToBattlefield(player1, new NassariDeanOfExpression());
        SerpentineCurve curve = new SerpentineCurve();
        harness.setLibrary(player2, List.of(curve, new SerpentineCurve()));

        triggerUpkeep(player1);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThat(gd.findExiledCard(curve.getId())).isNotNull();
        assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(curve.getId());
        assertThat(gd.exilePlayAnyManaTypeCardIds()).doesNotContain(curve.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, curve.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareLastCounterCast(Card card) {
        harness.setExile(player1, List.of(card));
        gd.setExiledCardCounters(card.getId(), CounterType.REFINE, 1);
    }

    private void offerLastCounterCast() {
        triggerUpkeep(player1);
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private Permanent addReadyUvilda() {
        Permanent uvilda = harness.addToBattlefieldAndReturn(player1, new UvildaDeanOfPerfection());
        uvilda.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return uvilda;
    }

    private void triggerUpkeep(com.github.laxika.magicalvibes.model.Player player) {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.forceActivePlayer(player);
            harness.forceStep(TurnStep.UNTAP);
            harness.passUntil(TurnStep.UPKEEP);
            harness.passBothPriorities();
        });
    }
}
