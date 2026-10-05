package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({JeskaiMonument.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class JeskaiMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability searches for an Island, Mountain, or Plains")
    void searchesForAJeskaiBasicLand() {
        harness.setHand(player1, List.of(new JeskaiMonument()));
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Island(), new Mountain(), new Plains(), new Forest(), new Swamp())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Island", "Mountain", "Plains");

        String chosenName = search.params().cards().getFirst().getName();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, chosenName);
    }

    @Test
    @DisplayName("Sacrificing the monument creates two 1/1 white Birds with flying")
    void sacrificeCreatesTwoFlyingBirds() {
        harness.addToBattlefield(player1, new JeskaiMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Jeskai Monument");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jeskai Monument");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Bird"))
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BIRD);
                    assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
                });
    }

    @Test
    @DisplayName("The token ability can be activated only at sorcery speed")
    void onlyAtSorcerySpeed() {
        harness.addToBattlefield(player1, new JeskaiMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayFailToFindEvenWhenAnEligibleLandExists() {
        harness.setHand(player1, List.of(new JeskaiMonument()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchWithNoEligibleLandsFinishesWithoutTakingACard() {
        harness.setHand(player1, List.of(new JeskaiMonument()));
        harness.setLibrary(player1, List.of(new Forest(), new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Swamp");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tappedMonumentCannotPayTheActivationCost() {
        harness.addToBattlefieldAndReturn(player1, new JeskaiMonument()).tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Jeskai Monument");
        harness.assertNotInGraveyard(player1, "Jeskai Monument");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringCombatOnYourOwnTurn() {
        harness.addToBattlefield(player1, new JeskaiMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Jeskai Monument");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileAnotherSpellIsOnTheStack() {
        harness.addToBattlefield(player1, new JeskaiMonument());
        harness.setHand(player1, List.of(new JeskaiMonument()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Jeskai Monument");
        harness.assertNotInGraveyard(player1, "Jeskai Monument");
        assertThat(gd.stack).hasSize(1);
    }
}
