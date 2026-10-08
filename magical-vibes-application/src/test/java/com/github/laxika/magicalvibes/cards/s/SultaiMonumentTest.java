package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SultaiMonument.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class SultaiMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability searches for a Swamp, Forest, or Island")
    void searchesForASultaiBasicLand() {
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Swamp(), new Forest(), new Island(), new Mountain(), new Plains())));
        harness.castFromHand(player1, new SultaiMonument(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Swamp", "Forest", "Island");

        String chosenName = search.params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, chosenName);
    }

    @Test
    @DisplayName("Sacrificing the monument creates two 2/2 black Zombie Druids")
    void sacrificeCreatesTwoZombieDruids() {
        harness.addToBattlefield(player1, new SultaiMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sultai Monument");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Zombie Druid"))
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(2);
                    assertThat(token.getEffectiveToughness()).isEqualTo(2);
                    assertThat(token.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.DRUID);
                });
    }

    @Test
    @DisplayName("The token ability can be activated only at sorcery speed")
    void onlyAtSorcerySpeed() {
        harness.addToBattlefield(player1, new SultaiMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canFailToFindEvenWhenAnEligibleLandExists() {
        harness.setLibrary(player1, List.of(new Swamp(), new Forest()));
        harness.castFromHand(player1, new SultaiMonument(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Sultai Monument");
    }

    @Test
    void searchCompletesWhenNoEligibleLandExists() {
        harness.setLibrary(player1, List.of(new Mountain(), new Plains()));
        harness.castFromHand(player1, new SultaiMonument(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Sultai Monument");
    }

    @Test
    void sacrificeIsPaidBeforeTokensResolve() {
        harness.addToBattlefield(player1, new SultaiMonument());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Sultai Monument");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(token.isTapped()).isFalse();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new SultaiMonument());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Sultai Monument");
        harness.assertNotInGraveyard(player1, "Sultai Monument");
    }

    @Test
    void cannotActivateWithASpellOnTheStack() {
        harness.addToBattlefield(player1, new SultaiMonument());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SultaiMonument(), "{2}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Sultai Monument");
        harness.assertNotInGraveyard(player1, "Sultai Monument");
        assertThat(gd.stack).hasSize(1);
    }
}
