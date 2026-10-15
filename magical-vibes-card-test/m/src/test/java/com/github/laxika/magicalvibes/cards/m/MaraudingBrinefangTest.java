package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaraudingBrinefang.class, Island.class, Forest.class, GrizzlyBears.class, Shock.class})
class MaraudingBrinefangTest extends BaseCardTest {

    @Test
    @DisplayName("Islandcycling discards the card and offers only Islands")
    void islandcyclingDiscardsAndSearchesForIslands() {
        harness.setHand(player1, List.of(new MaraudingBrinefang()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Marauding Brinefang");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getSubtypes().contains(CardSubtype.ISLAND))
                .hasSize(1);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent brinefang = addBrinefang();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, brinefang.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Marauding Brinefang");
    }

    @Test
    @DisplayName("Paying Ward lets an opponent's targeted spell resolve")
    void wardCanBePaid() {
        Permanent brinefang = addBrinefang();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstant(player2, 0, brinefang.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(brinefang.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Marauding Brinefang");
    }

    @Test
    @DisplayName("Islandcycling pays and discards before its search resolves")
    void islandcyclingPaysCostsImmediately() {
        harness.setHand(player1, List.of(new MaraudingBrinefang()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Marauding Brinefang");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Islandcycling resolves without drawing when no Island is available")
    void islandcyclingWithoutIsland() {
        harness.setHand(player1, List.of(new MaraudingBrinefang()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Marauding Brinefang");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent may decline ward even with enough mana")
    void wardPaymentCanBeDeclined() {
        Permanent brinefang = addBrinefang();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstant(player2, 0, brinefang.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(brinefang.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Marauding Brinefang");
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's spell")
    void wardDoesNotTaxController() {
        Permanent brinefang = harness.addToBattlefieldAndReturn(player1, new MaraudingBrinefang());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, brinefang.getId());
        harness.passBothPriorities();

        assertThat(brinefang.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addBrinefang() {
        Permanent brinefang = harness.addToBattlefieldAndReturn(player1, new MaraudingBrinefang());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return brinefang;
    }
}
