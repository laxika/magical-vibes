package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.s.SpectralSteel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AxgardArmoryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and taps for white mana")
    void entersTappedAndTapsForWhite() {
        harness.setHand(player1, List.of(new AxgardArmory()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Axgard Armory").isTapped()).isTrue();

        findPermanent(player1, "Axgard Armory").untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Searches for one Aura and one Equipment, then shuffles")
    void searchesForAuraAndEquipment() {
        Card aura = new SpectralSteel();
        Card equipment = new ShortSword();
        harness.addToBattlefield(player1, new AxgardArmory());
        harness.setLibrary(player1, List.of(aura, equipment, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch auraSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(auraSearch.params().cards()).extracting(Card::getId).containsExactly(aura.getId());
        assertThat(auraSearch.params().reveals()).isTrue();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch equipmentSearch = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(equipmentSearch.params().cards()).extracting(Card::getId).containsExactly(equipment.getId());

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(aura.getId(), equipment.getId());
        harness.assertInGraveyard(player1, "Axgard Armory");
    }

    @Test
    @DisplayName("Finds an Equipment when no Aura is in the library")
    void findsEquipmentWithoutAura() {
        Card equipment = new ShortSword();
        harness.addToBattlefield(player1, new AxgardArmory());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), equipment));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getId).containsExactly(equipment.getId());
        assertThat(search.params().cards()).allMatch(card -> card.getSubtypes().contains(CardSubtype.EQUIPMENT));

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).containsExactly(equipment.getId());
    }
}
