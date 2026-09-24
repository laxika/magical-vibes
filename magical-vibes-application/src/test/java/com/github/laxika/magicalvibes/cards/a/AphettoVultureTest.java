package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfMalice;
import com.github.laxika.magicalvibes.cards.g.GluttonousZombie;
import com.github.laxika.magicalvibes.cards.s.SliceAndDice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AphettoVulture.class, GluttonousZombie.class, DiscipleOfMalice.class, SliceAndDice.class})
class AphettoVultureTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, returns a targeted Zombie card to the top of its controller's library")
    void deathTriggerPutsTargetedZombieOnTopOfLibrary() {
        AphettoVulture vulture = new AphettoVulture();
        Card zombie = new GluttonousZombie();
        Card nonZombie = new DiscipleOfMalice();
        harness.addToBattlefield(player1, vulture);
        harness.setGraveyard(player1, new ArrayList<>(List.of(zombie, nonZombie)));
        harness.setLibrary(player1, new ArrayList<>());

        destroyVulture();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).contains(zombie.getId(), vulture.getId());
        assertThat(choice.validCardIds()).doesNotContain(nonZombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of(zombie.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(zombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vulture, nonZombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(zombie);
    }

    @Test
    @DisplayName("The death trigger may be declined")
    void deathTriggerMayBeDeclined() {
        AphettoVulture vulture = new AphettoVulture();
        Card zombie = new GluttonousZombie();
        harness.addToBattlefield(player1, vulture);
        harness.setGraveyard(player1, new ArrayList<>(List.of(zombie)));
        harness.setLibrary(player1, new ArrayList<>());

        destroyVulture();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(zombie.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vulture, zombie);
    }

    @Test
    @DisplayName("The death trigger can target only a Zombie card in its controller's graveyard")
    void deathTriggerTargetsOnlyOwnGraveyard() {
        AphettoVulture vulture = new AphettoVulture();
        Card opponentZombie = new GluttonousZombie();
        Card ownNonZombie = new DiscipleOfMalice();
        harness.addToBattlefield(player1, vulture);
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownNonZombie)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentZombie)));
        harness.setLibrary(player1, new ArrayList<>());

        destroyVulture();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(vulture.getId());
        assertThat(choice.validCardIds()).doesNotContain(opponentZombie.getId(), ownNonZombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of(vulture.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(vulture);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentZombie);
    }

    @Test
    @DisplayName("The death trigger does not resolve when its targeted Zombie leaves the graveyard")
    void deathTriggerFizzlesIfTargetLeavesGraveyard() {
        AphettoVulture vulture = new AphettoVulture();
        Card zombie = new GluttonousZombie();
        harness.addToBattlefield(player1, vulture);
        harness.setGraveyard(player1, new ArrayList<>(List.of(zombie)));
        harness.setLibrary(player1, new ArrayList<>());

        destroyVulture();

        harness.handleMultipleCardsChosen(player1, List.of(zombie.getId()));
        List<Card> remainingGraveyard = new ArrayList<>(gd.playerGraveyards.get(player1.getId()));
        remainingGraveyard.remove(zombie);
        harness.setGraveyard(player1, remainingGraveyard);
        harness.setExile(player1, List.of(zombie));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().equals(zombie));
    }

    private void destroyVulture() {
        harness.castFromHand(player1, new SliceAndDice(), "{4}{R}{R}");
        harness.passBothPriorities();
    }
}
