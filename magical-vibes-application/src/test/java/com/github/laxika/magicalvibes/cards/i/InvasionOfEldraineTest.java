package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.k.KhenraSpellspear;
import com.github.laxika.magicalvibes.cards.p.PrickleFaeries;
import com.github.laxika.magicalvibes.cards.t.TimberlandAncient;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvasionOfEldraine.class, PrickleFaeries.class, TimberlandAncient.class, KhenraSpellspear.class})
class InvasionOfEldraineTest extends BaseCardTest {

    @Test
    void entersAndMakesTargetOpponentDiscardTwoCards() {
        harness.setHand(player2, List.of(new TimberlandAncient(), new TimberlandAncient()));
        harness.setHand(player1, List.of(new InvasionOfEldraine()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void defeatCastsPrickleFaeriesTransformed() {
        defeatBattleAndAcceptCast();
        resolveAllTriggers();

        Permanent faeries = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof PrickleFaeries)
                .findFirst()
                .orElseThrow();
        assertThat(faeries.isTransformed()).isTrue();
    }

    @Test
    void prickleFaeriesDealsDamageOnOpponentUpkeepWithTwoOrFewerCards() {
        harness.addToBattlefield(player1, new PrickleFaeries());
        harness.setHand(player2, List.of(new TimberlandAncient(), new TimberlandAncient()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void discardsOnlyAvailableCardWhenOpponentHasOneCard() {
        harness.setHand(player2, List.of(new TimberlandAncient()));
        harness.setHand(player1, List.of(new InvasionOfEldraine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyOpponentHandDoesNotRequireDiscardChoice() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new InvasionOfEldraine()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Invasion of Eldraine");
    }

    @Test
    void prickleFaeriesDoesNotTriggerWithThreeCardsEvenIfHandShrinksLater() {
        harness.addToBattlefield(player1, new PrickleFaeries());
        harness.setHand(player2, List.of(new TimberlandAncient(), new TimberlandAncient(),
                new TimberlandAncient()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player2, List.of());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void prickleFaeriesRechecksHandSizeWhenAbilityResolves() {
        harness.addToBattlefield(player1, new PrickleFaeries());
        harness.setHand(player2, List.of(new TimberlandAncient(), new TimberlandAncient()));
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new TimberlandAncient(), new TimberlandAncient(),
                new TimberlandAncient()));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void prickleFaeriesDamagesOpponentWithEmptyHand() {
        harness.addToBattlefield(player1, new PrickleFaeries());
        harness.setHand(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void prickleFaeriesDoesNotTriggerDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new PrickleFaeries());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void controllerMayDeclineCastingDefeatedBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfEldraine());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Invasion of Eldraine");
        harness.assertNotOnBattlefield(player1, "Prickle Faeries");
        assertThat(gd.findExiledCard(battle.getCard().getId())).isNotNull();
    }

    @Test
    void castingPrickleFaeriesDoesNotTriggerProwess() {
        Permanent spellspear = harness.addToBattlefieldAndReturn(player1, new KhenraSpellspear());
        int powerBefore = gqs.getEffectivePower(gd, spellspear);
        int toughnessBefore = gqs.getEffectiveToughness(gd, spellspear);

        defeatBattleAndAcceptCast();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Prickle Faeries");
        assertThat(gqs.getEffectivePower(gd, spellspear)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, spellspear)).isEqualTo(toughnessBefore);
    }

    @Test
    void defeatedBattleIsCastFromExileRatherThanGraveyard() {
        defeatBattleAndAcceptCast();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Prickle Faeries");
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId(), Zone.EXILE)).isEqualTo(1);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId(), Zone.GRAVEYARD)).isZero();
    }

    @Test
    void opponentChoosesTwoCardsAndKeepsRemainingCard() {
        TimberlandAncient first = new TimberlandAncient();
        TimberlandAncient kept = new TimberlandAncient();
        TimberlandAncient last = new TimberlandAncient();
        harness.setHand(player2, List.of(first, kept, last));
        harness.setHand(player1, List.of(new InvasionOfEldraine(), new TimberlandAncient()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, last);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void defeatBattleAndAcceptCast() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfEldraine());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }
}
