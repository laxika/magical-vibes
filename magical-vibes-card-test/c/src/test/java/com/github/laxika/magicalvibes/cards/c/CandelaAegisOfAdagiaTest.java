package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AxavarFateThief;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.s.StoicStarCaptain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CandelaAegisOfAdagia.class, GrizzlyBears.class, AxavarFateThief.class,
        StoicStarCaptain.class, EnsoulArtifact.class})
class CandelaAegisOfAdagiaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a creature you control and its card keeps the perpetual boost")
    void etbReturnsAndPerpetuallyBoostsCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CandelaAegisOfAdagia()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        resolveAllTriggers();

        Card returnedCard = bears.getCard();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(returnedCard.getId());
        assertThat(gd.perpetualCardPowerToughnessModifiers).containsKey(returnedCard.getId());

        harness.setHand(player1, List.of(returnedCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent recast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == returnedCard)
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, recast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recast)).isEqualTo(3);
    }

    @Test
    @DisplayName("Station animates Candela and grants flying at eight charge counters")
    void stationUnlocksFlying() {
        Permanent candela = harness.addToBattlefieldAndReturn(player1, new CandelaAegisOfAdagia());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(candela), null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(candela.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        candela.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, candela)).isTrue();
        assertThat(gqs.hasKeyword(gd, candela, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Combat damage offers a creature card with mana value three or less")
    void combatDamageOffersEligibleCreature() {
        Permanent candela = addCreatureReady(player1, new CandelaAegisOfAdagia());
        candela.setCounterCount(CounterType.CHARGE, 8);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() == bears);
    }

    @Test
    @DisplayName("Declining the combat-damage card choice leaves the card in hand")
    void combatDamageMayBeDeclined() {
        Permanent candela = addCreatureReady(player1, new CandelaAegisOfAdagia());
        candela.setCounterCount(CounterType.CHARGE, 8);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == bears);
    }

    @Test
    void etbMayChooseNoTargetEvenWithACreatureAvailable() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CandelaAegisOfAdagia()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Candela, Aegis of Adagia");
    }

    @Test
    void etbCannotTargetAnOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CandelaAegisOfAdagia()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stationCanTapASummoningSickCreature() {
        Permanent candela = harness.addToBattlefieldAndReturn(player1, new CandelaAegisOfAdagia());
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new StoicStarCaptain());
        captain.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(captain.isTapped()).isTrue();
        assertThat(candela.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void stationCannotTapCandelaItself() {
        Permanent candela = addCreatureReady(player1, new CandelaAegisOfAdagia());
        candela.setCounterCount(CounterType.CHARGE, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(candela.isTapped()).isFalse();
    }

    @Test
    void stationCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new CandelaAegisOfAdagia());
        addCreatureReady(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stationAbilitiesDisappearBelowEightCounters() {
        Permanent candela = harness.addToBattlefieldAndReturn(player1, new CandelaAegisOfAdagia());
        candela.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, candela)).isTrue();
        assertThat(gqs.hasKeyword(gd, candela, Keyword.FLYING)).isTrue();

        candela.setCounterCount(CounterType.CHARGE, 7);

        assertThat(gqs.isCreature(gd, candela)).isFalse();
        assertThat(gqs.hasKeyword(gd, candela, Keyword.FLYING)).isFalse();
    }

    @Test
    void combatDamageExcludesExpensiveCreaturesAndNoncreatureCards() {
        Permanent candela = addCreatureReady(player1, new CandelaAegisOfAdagia());
        candela.setCounterCount(CounterType.CHARGE, 8);
        StoicStarCaptain captain = new StoicStarCaptain();
        harness.setHand(player1, List.of(new AxavarFateThief(), new CandelaAegisOfAdagia(), captain));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(2);
        harness.handleCardChosen(player1, 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() == captain);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void animatedCandelaBelowEightCountersHasNoCombatDamageAbility() {
        Permanent candela = addCreatureReady(player1, new CandelaAegisOfAdagia());
        candela.setCounterCount(CounterType.CHARGE, 7);
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, candela.getId());
        resolveAllTriggers();
        StoicStarCaptain captain = new StoicStarCaptain();
        harness.setHand(player1, List.of(captain));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(captain);
    }

    @Test
    void flashAllowsCastingDuringOpponentsCombatWithNoTarget() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new CandelaAegisOfAdagia()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Candela, Aegis of Adagia");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void combatDamageWithNoEligibleCardDoesNotRequireAChoice() {
        Permanent candela = addCreatureReady(player1, new CandelaAegisOfAdagia());
        candela.setCounterCount(CounterType.CHARGE, 8);
        AxavarFateThief axavar = new AxavarFateThief();
        harness.setHand(player1, List.of(axavar));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(axavar);
    }
}
