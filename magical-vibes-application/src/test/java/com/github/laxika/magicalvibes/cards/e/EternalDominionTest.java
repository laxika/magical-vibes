package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EternalDominionTest extends BaseCardTest {

    @Test
    @DisplayName("Offers artifact, creature, enchantment, and land cards from the target library")
    void offersPermanentCardsFromTargetLibrary() {
        harness.setLibrary(player2, List.of(
                new Ornithopter(), new GrizzlyBears(), new Pacifism(), new Forest(), new Divination()));
        castEternalDominion();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting("name")
                .containsExactlyInAnyOrder("Ornithopter", "Grizzly Bears", "Pacifism", "Forest");
    }

    @Test
    @DisplayName("Puts the chosen card onto the battlefield under your control and applies Epic")
    void putsChosenCardUnderControlAndAppliesEpic() {
        harness.setLibrary(player2, List.of(new Forest(), new Divination()));
        castEternalDominion();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).extracting("name").containsExactly("Divination");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Copies the spell at each upkeep and keeps the original opponent target when declined")
    void copiesSpellAtEachUpkeep() {
        harness.setLibrary(player2, List.of(new Forest(), new Divination()));
        castEternalDominion();
        chooseLibraryCard(0);

        harness.setLibrary(player2, List.of(new Ornithopter(), new Divination()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting("name").containsExactly("Ornithopter");

        chooseLibraryCard(0);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactlyInAnyOrder("Forest", "Ornithopter");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new EternalDominion()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castEternalDominion() {
        harness.setHand(player1, List.of(new EternalDominion()));
        harness.addMana(player1, ManaColor.BLUE, 10);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void chooseLibraryCard(int index) {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
