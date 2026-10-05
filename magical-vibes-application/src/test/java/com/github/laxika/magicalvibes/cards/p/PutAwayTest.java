package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.ScuzzbackScrapper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PutAway.class, ScuzzbackScrapper.class})
class PutAwayTest extends BaseCardTest {

    private Card player1CastsCreature() {
        Card creature = new ScuzzbackScrapper();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return creature;
    }

    private void giveP2PutAway() {
        harness.setHand(player2, List.of(new PutAway()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Counters the spell and optionally shuffles the card targeted when cast")
    void countersAndShufflesChosenCard() {
        Card buried = new ScuzzbackScrapper();
        harness.setGraveyard(player2, List.of(buried));
        int libSizeBefore = gd.playerDecks.get(player2.getId()).size();
        Card creature = player1CastsCreature();
        giveP2PutAway();

        harness.castAndResolveInstant(player2, 0, List.of(creature.getId(), buried.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player1, "Scuzzback Scrapper");
        harness.assertInGraveyard(player1, "Scuzzback Scrapper");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(buried);
        assertThat(gd.playerDecks.get(player2.getId())).contains(buried).hasSize(libSizeBefore + 1);
    }

    @Test
    @DisplayName("Omitting the optional graveyard target leaves the graveyard untouched")
    void countersAndDeclineLeavesGraveyard() {
        Card buried = new ScuzzbackScrapper();
        harness.setGraveyard(player2, List.of(buried));
        int libSizeBefore = gd.playerDecks.get(player2.getId()).size();
        Card creature = player1CastsCreature();
        giveP2PutAway();

        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Scuzzback Scrapper");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(buried);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libSizeBefore);
    }

    @Test
    @DisplayName("May decline to shuffle the graveyard target after countering the spell")
    void mayDeclineToShuffleTargetedCard() {
        Card buried = new ScuzzbackScrapper();
        harness.setGraveyard(player2, List.of(buried));
        int libSizeBefore = gd.playerDecks.get(player2.getId()).size();
        Card creature = player1CastsCreature();
        giveP2PutAway();

        harness.castAndResolveInstant(player2, 0, List.of(creature.getId(), buried.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Scuzzback Scrapper");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(buried);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libSizeBefore);
    }

    @Test
    @DisplayName("Counters the spell with an empty graveyard without prompting")
    void countersWithEmptyGraveyard() {
        Card creature = player1CastsCreature();
        giveP2PutAway();

        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Scuzzback Scrapper");
        harness.assertInGraveyard(player2, "Put Away");
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a spell on the stack")
    void cannotTargetPermanent() {
        player1CastsCreature();
        harness.addToBattlefield(player1, new ScuzzbackScrapper());
        UUID permanentId = harness.getPermanentId(player1, "Scuzzback Scrapper");
        giveP2PutAway();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Countering your own spell cannot make it a new graveyard target during resolution")
    void cannotShuffleTheSpellItJustCountered() {
        Card creature = player1CastsCreature();
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new PutAway()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Scuzzback Scrapper");
        harness.assertInGraveyard(player1, "Put Away");
    }
}
