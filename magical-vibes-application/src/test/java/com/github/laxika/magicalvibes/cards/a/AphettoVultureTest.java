package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CemeteryReaper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AphettoVulture.class, CemeteryReaper.class, GrizzlyBears.class, WrathOfGod.class})
class AphettoVultureTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, returns a targeted Zombie card to the top of its controller's library")
    void deathTriggerPutsTargetedZombieOnTopOfLibrary() {
        AphettoVulture vulture = new AphettoVulture();
        Card zombie = new CemeteryReaper();
        Card nonZombie = new GrizzlyBears();
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

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(zombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vulture, nonZombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(zombie);
    }

    @Test
    @DisplayName("The death trigger may be declined")
    void deathTriggerMayBeDeclined() {
        AphettoVulture vulture = new AphettoVulture();
        Card zombie = new CemeteryReaper();
        harness.addToBattlefield(player1, vulture);
        harness.setGraveyard(player1, new ArrayList<>(List.of(zombie)));
        harness.setLibrary(player1, new ArrayList<>());

        destroyVulture();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vulture, zombie);
    }

    private void destroyVulture() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
