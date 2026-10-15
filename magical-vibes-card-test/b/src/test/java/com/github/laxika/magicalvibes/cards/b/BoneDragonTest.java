package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoneDragon.class, WalkingCorpse.class})
class BoneDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability returns Bone Dragon to the battlefield tapped, exiling seven other cards")
    void graveyardAbilityReturnsSelfTapped() {
        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            graveyard.add(new WalkingCorpse());
        }
        graveyard.add(new BoneDragon());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbility(player1, 7);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bone Dragon");
        assertThat(findPermanent(player1, "Bone Dragon").isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .hasSize(7)
                .noneMatch(c -> c.getName().equals("Bone Dragon"));
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated without seven other cards to exile")
    void graveyardAbilityRequiresSevenOtherCards() {
        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            graveyard.add(new WalkingCorpse());
        }
        graveyard.add(new BoneDragon());
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 6))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Bone Dragon");
    }

    @Test
    @DisplayName("Exile costs are paid before resolution even when the source is first in the graveyard")
    void paysExileCostBeforeResolving() {
        BoneDragon dragon = new BoneDragon();
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(dragon);
        for (int i = 0; i < 7; i++) {
            graveyard.add(new WalkingCorpse());
        }
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(dragon);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(7);
        harness.assertNotOnBattlefield(player1, "Bone Dragon");

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bone Dragon").getCard()).isSameAs(dragon);
        assertThat(findPermanent(player1, "Bone Dragon").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The player chooses seven other cards and only the activating Bone Dragon returns")
    void choosesExileCardsAndReturnsOnlySource() {
        BoneDragon dragon = new BoneDragon();
        BoneDragon otherDragon = new BoneDragon();
        List<Card> graveyard = new ArrayList<>(List.of(dragon, otherDragon));
        List<Card> payment = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            payment.add(new WalkingCorpse());
        }
        graveyard.addAll(payment);
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleMultipleCardsChosen(player1, payment.stream().map(Card::getId).toList());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherDragon);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(payment);
        assertThat(countPermanents(player1, "Bone Dragon")).isEqualTo(1);
        assertThat(findPermanent(player1, "Bone Dragon").getCard()).isSameAs(dragon);
    }

    @Test
    @DisplayName("Insufficient black mana prevents activation without exiling any cards")
    void requiresTwoBlackMana() {
        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new BoneDragon());
        for (int i = 0; i < 7; i++) {
            graveyard.add(new WalkingCorpse());
        }
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Bone Dragon");
    }
}
