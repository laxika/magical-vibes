package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RenownedWeaponsmithTest extends BaseCardTest {

    private void setUpWeaponsmith() {
        harness.addToBattlefield(player1, new RenownedWeaponsmith());
        findPermanent(player1, "Renowned Weaponsmith").setSummoningSick(false);
    }

    private Card namedCard(String name) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        return card;
    }

    @Test
    @DisplayName("Tapping Renowned Weaponsmith adds two artifact-restricted colorless mana")
    void addsArtifactRestrictedMana() {
        setUpWeaponsmith();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("Artifact-restricted mana can pay for an artifact spell")
    void restrictedManaCanPayForArtifactSpell() {
        setUpWeaponsmith();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CopperMyr()));

        harness.activateAbility(player1, 0, null, null);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("Artifact-restricted mana cannot pay for a nonartifact spell")
    void restrictedManaCannotPayForNonartifactSpell() {
        setUpWeaponsmith();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Second ability offers only Heart-Piercer Bow and Vial of Dragonfire")
    void searchOffersOnlyNamedCards() {
        setUpWeaponsmith();
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(
                namedCard("Heart-Piercer Bow"),
                namedCard("Vial of Dragonfire"),
                namedCard("Other Artifact")));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Heart-Piercer Bow", "Vial of Dragonfire");
    }

    @Test
    @DisplayName("Second ability puts the chosen card into hand")
    void chosenCardGoesToHand() {
        setUpWeaponsmith();
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(namedCard("Vial of Dragonfire"));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Vial of Dragonfire");
    }
}
