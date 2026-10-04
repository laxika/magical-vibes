package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtherswornSphinx.class, Spellbook.class, HillGiant.class, Mountain.class})
class EtherswornSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new EtherswornSphinx()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player2, new Spellbook());
        }
        harness.setHand(player1, List.of(new EtherswornSphinx()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cascade skips lands and offers the first lesser-mana-value nonland")
    void cascadeOffersFirstLesserNonland() {
        harness.setLibrary(player1, List.of(new Mountain(), new HillGiant()));
        harness.setHand(player1, List.of(new EtherswornSphinx()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Hill Giant");
    }

    @Test
    void affinityCannotReduceColoredManaRequirements() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new EtherswornSphinx()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void affinityDoesNotLowerCascadeManaValueAndEqualManaValueIsSkipped() {
        EtherswornSphinx equalManaValue = new EtherswornSphinx();
        HillGiant hit = new HillGiant();
        harness.setLibrary(player1, List.of(equalManaValue, hit));
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new EtherswornSphinx()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
    }

    @Test
    void cascadeCardsAreInExileWhileChoosingWhetherToCast() {
        Mountain land = new Mountain();
        HillGiant hit = new HillGiant();
        harness.setLibrary(player1, List.of(land, hit));
        harness.setHand(player1, List.of(new EtherswornSphinx()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(land, hit);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningCascadeReturnsExiledCardsBelowUnexaminedCards() {
        Mountain land = new Mountain();
        HillGiant hit = new HillGiant();
        Spellbook unexamined = new Spellbook();
        harness.setLibrary(player1, List.of(land, hit, unexamined));
        harness.setHand(player1, List.of(new EtherswornSphinx()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unexamined);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(unexamined, land, hit);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(hit.getId()));
    }

    @Test
    void cascadeCastsCreatureWithoutManaBeforeSphinxResolves() {
        Mountain land = new Mountain();
        HillGiant hit = new HillGiant();
        Spellbook unexamined = new Spellbook();
        harness.setLibrary(player1, List.of(land, hit, unexamined));
        harness.setHand(player1, List.of(new EtherswornSphinx()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(hit.getId())
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unexamined, land);
        harness.assertNotOnBattlefield(player1, "Ethersworn Sphinx");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Ethersworn Sphinx");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ethersworn Sphinx");
    }

    @Test
    void cascadeWithNoQualifyingCardReturnsAllCardsAndResolvesSphinx() {
        Mountain land = new Mountain();
        EtherswornSphinx equalManaValue = new EtherswornSphinx();
        harness.setLibrary(player1, List.of(land, equalManaValue));
        harness.setHand(player1, List.of(new EtherswornSphinx()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, equalManaValue);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ethersworn Sphinx");
    }
}
