package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TreasureKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger reveals until the first nonland card with mana value 3 or less")
    void revealsUntilFirstQualifyingCard() {
        TreasureKeeper keeper = new TreasureKeeper();
        HillGiant tooExpensive = new HillGiant();
        CounselOfTheSoratami hit = new CounselOfTheSoratami();
        GrizzlyBears belowHit = new GrizzlyBears();
        setUpDeathTrigger(keeper, List.of(new Plains(), tooExpensive, hit, belowHit));

        resolveDeathTrigger();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(hit);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowHit);
    }

    @Test
    @DisplayName("The qualifying card may be cast without paying its mana cost")
    void castsQualifyingCardForFree() {
        TreasureKeeper keeper = new TreasureKeeper();
        CounselOfTheSoratami hit = new CounselOfTheSoratami();
        setUpDeathTrigger(keeper, List.of(hit));
        int blueBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        resolveDeathTrigger();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.SORCERY_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(blueBefore);
    }

    @Test
    @DisplayName("If no qualifying card is found, all revealed cards go to the library bottom")
    void bottomsRevealedCardsWhenNoMatch() {
        TreasureKeeper keeper = new TreasureKeeper();
        Forest land = new Forest();
        HillGiant tooExpensive = new HillGiant();
        setUpDeathTrigger(keeper, List.of(land, tooExpensive));

        resolveDeathTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, tooExpensive);
    }

    private void setUpDeathTrigger(TreasureKeeper keeper, List<Card> library) {
        harness.addToBattlefield(player1, keeper);
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
