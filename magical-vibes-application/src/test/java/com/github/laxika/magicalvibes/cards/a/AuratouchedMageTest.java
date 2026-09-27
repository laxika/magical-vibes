package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CursedLand;
import com.github.laxika.magicalvibes.cards.m.MoldervineCloak;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuratouchedMage.class, CursedLand.class, MoldervineCloak.class})
class AuratouchedMageTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for an Aura that can enchant it and attaches the Aura")
    void searchesForAuraAndAttachesIt() {
        castMage();
        MoldervineCloak cloak = new MoldervineCloak();
        harness.setLibrary(player1, List.of(new CursedLand(), cloak));

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(cloak);
        assertThat(search.params().destination())
                .isEqualTo(com.github.laxika.magicalvibes.model.LibrarySearchDestination.BATTLEFIELD_ATTACHED_TO_PERMANENT);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent mage = findPermanent(player1, "Auratouched Mage");
        Permanent aura = findPermanent(player1, "Moldervine Cloak");
        assertThat(aura.getAttachedTo()).isEqualTo(mage.getId());
    }

    @Test
    @DisplayName("Puts the Aura into hand if it is no longer on the battlefield")
    void putsAuraIntoHandIfSourceLeaves() {
        castMage();
        harness.setLibrary(player1, List.of(new MoldervineCloak()));

        harness.passBothPriorities();
        Permanent mage = findPermanent(player1, "Auratouched Mage");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, mage));
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination())
                .isEqualTo(com.github.laxika.magicalvibes.model.LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Moldervine Cloak");
        harness.assertInHand(player1, "Auratouched Mage");
        harness.assertNotOnBattlefield(player1, "Auratouched Mage");
    }

    @Test
    @DisplayName("Does not offer an Aura that cannot enchant it")
    void filtersOutIneligibleAuras() {
        castMage();
        CursedLand cursedLand = new CursedLand();
        harness.setLibrary(player1, List.of(cursedLand));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertOnBattlefield(player1, "Auratouched Mage");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cursedLand);
    }

    private void castMage() {
        harness.setHand(player1, List.of(new AuratouchedMage()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }

}
