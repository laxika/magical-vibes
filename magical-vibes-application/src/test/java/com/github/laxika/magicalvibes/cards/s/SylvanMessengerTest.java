package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.e.ElvishPromenade;
import com.github.laxika.magicalvibes.cards.i.Index;
import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanMessenger.class, UrborgElf.class, Dodecapod.class, CavesOfKoilos.class,
        Index.class, WoodlandChangeling.class, ElvishPromenade.class})
class SylvanMessengerTest extends BaseCardTest {

    private void finishAnyReorder() {
        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        if (reorder != null) {
            harness.getGameService().handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(IntStream.range(0, reorder.cards().size()).boxed().toList()));
        }
    }

    private void castMessenger() {
        harness.castFromHand(player1, new SylvanMessenger(), "{3}{G}");
        harness.passBothPriorities();  // resolve Sylvan Messenger -> ETB trigger queued
        harness.passBothPriorities();  // resolve the reveal trigger
    }

    @Test
    @DisplayName("Elf cards among the top four go to hand, the rest to the bottom")
    void elvesGoToHand() {
        SylvanMessenger elf1 = new SylvanMessenger();
        UrborgElf elf2 = new UrborgElf();
        Dodecapod nonElf = new Dodecapod();
        CavesOfKoilos land = new CavesOfKoilos();
        Index sorcery = new Index();
        harness.setLibrary(player1, List.of(elf1, elf2, nonElf, land, sorcery));

        castMessenger();
        finishAnyReorder();

        var deck = gd.playerDecks.get(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(elf1, elf2);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonElf, land);
        assertThat(deck).contains(nonElf, land, sorcery);
    }

    @Test
    @DisplayName("Only the top four cards are revealed — a fifth Elf stays in the library")
    void onlyTopFourAreRevealed() {
        Dodecapod nonElf1 = new Dodecapod();
        CavesOfKoilos nonElf2 = new CavesOfKoilos();
        Index nonElf3 = new Index();
        Dodecapod nonElf4 = new Dodecapod();
        UrborgElf deepElf = new UrborgElf();
        harness.setLibrary(player1, List.of(nonElf1, nonElf2, nonElf3, nonElf4, deepElf));

        castMessenger();
        finishAnyReorder();

        var deck = gd.playerDecks.get(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(deepElf);
        assertThat(deck).contains(deepElf);
    }

    @Test
    @DisplayName("A changeling card counts as an Elf card")
    void changelingCountsAsElf() {
        WoodlandChangeling changeling = new WoodlandChangeling();
        Index nonElf = new Index();
        harness.setLibrary(player1, List.of(changeling, nonElf));

        castMessenger();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonElf);
    }

    @Test
    @DisplayName("A non-creature Elf card is still put into hand")
    void nonCreatureElfCardGoesToHand() {
        ElvishPromenade elfTribal = new ElvishPromenade();
        harness.setLibrary(player1, List.of(elfTribal));

        castMessenger();
        finishAnyReorder();

        assertThat(gd.playerHands.get(player1.getId())).contains(elfTribal);
    }

    @Test
    @DisplayName("An empty library does not create a reorder interaction")
    void emptyLibraryDoesNotCreateReorderInteraction() {
        harness.setLibrary(player1, List.of());

        castMessenger();

        harness.assertOnBattlefield(player1, "Sylvan Messenger");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
