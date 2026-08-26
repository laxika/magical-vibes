package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimordialGnawer.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, WrathOfGod.class})
class PrimordialGnawerTest extends BaseCardTest {

    @Test
    @DisplayName("When Primordial Gnawer dies, discover 3")
    void discoversThreeWhenItDies() {
        PrimordialGnawer gnawer = new PrimordialGnawer();
        HillGiant tooExpensive = new HillGiant();
        CounselOfTheSoratami discovered = new CounselOfTheSoratami();
        GrizzlyBears belowDiscovered = new GrizzlyBears();
        Forest land = new Forest();
        setUpDeathTrigger(gnawer, List.of(land, tooExpensive, discovered, belowDiscovered));

        resolveDeathTrigger();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).contains(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, tooExpensive,
                belowDiscovered);
    }

    @Test
    @DisplayName("Discover 3 can cast the found card without paying its mana cost")
    void castsDiscoveredCardForFree() {
        PrimordialGnawer gnawer = new PrimordialGnawer();
        CounselOfTheSoratami discovered = new CounselOfTheSoratami();
        setUpDeathTrigger(gnawer, List.of(discovered));
        int blueBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        resolveDeathTrigger();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == discovered
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(blueBefore);
    }

    private void setUpDeathTrigger(PrimordialGnawer gnawer, List<Card> library) {
        harness.addToBattlefield(player1, gnawer);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
    }

    private void resolveDeathTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
