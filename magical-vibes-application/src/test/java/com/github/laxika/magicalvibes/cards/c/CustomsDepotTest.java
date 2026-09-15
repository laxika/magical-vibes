package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KyrenToy;
import com.github.laxika.magicalvibes.cards.s.SteadfastGuard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CustomsDepot.class, SteadfastGuard.class, KyrenToy.class})
class CustomsDepotTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature offers to pay {1} to draw and discard")
    void creatureSpellOffersPaidLoot() {
        SteadfastGuard cast = new SteadfastGuard();
        SteadfastGuard kept = new SteadfastGuard();
        KyrenToy drawn = new KyrenToy();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new CustomsDepot());
        harness.setHand(player1, new ArrayList<>(List.of(cast, kept)));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(kept);
    }

    @Test
    @DisplayName("Declining the payment does not draw or discard")
    void decliningPaymentDoesNothing() {
        SteadfastGuard cast = new SteadfastGuard();
        SteadfastGuard kept = new SteadfastGuard();
        KyrenToy drawn = new KyrenToy();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new CustomsDepot());
        harness.setHand(player1, new ArrayList<>(List.of(cast, kept)));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Casting a noncreature spell does not trigger Customs Depot")
    void nonCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new CustomsDepot());
        harness.setHand(player1, List.of(new KyrenToy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("An opponent's creature spell does not trigger Customs Depot")
    void opponentCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new CustomsDepot());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SteadfastGuard()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }
}
