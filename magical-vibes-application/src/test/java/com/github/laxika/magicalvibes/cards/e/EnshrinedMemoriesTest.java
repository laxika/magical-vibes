package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BodyOfJukai;
import com.github.laxika.magicalvibes.cards.f.ForkedBranchGarami;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.s.SickeningShoal;
import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        EnshrinedMemories.class,
        BodyOfJukai.class,
        ForkedBranchGarami.class,
        SickeningShoal.class,
        TendoIceBridge.class,
        GodsEyeGateToTheReikai.class
})
class EnshrinedMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals X cards, puts every creature into hand, and bottoms the rest")
    void putsAllRevealedCreaturesIntoHand() {
        Card firstCreature = new BodyOfJukai();
        Card firstNoncreature = new SickeningShoal();
        Card secondCreature = new ForkedBranchGarami();
        Card secondNoncreature = new TendoIceBridge();
        Card untouched = new GodsEyeGateToTheReikai();
        harness.setLibrary(player1,
                List.of(firstCreature, firstNoncreature, secondCreature, secondNoncreature, untouched));

        harness.setHand(player1, List.of(new EnshrinedMemories()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstCreature, secondCreature);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(firstNoncreature, secondNoncreature);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouched, secondNoncreature, firstNoncreature);
    }

    @Test
    @DisplayName("Only cards within X are revealed")
    void onlyLooksAtPaidXCards() {
        Card creature = new BodyOfJukai();
        Card noncreature = new SickeningShoal();
        Card outsideX = new ForkedBranchGarami();
        harness.setLibrary(player1, List.of(creature, noncreature, outsideX));

        harness.setHand(player1, List.of(new EnshrinedMemories()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId()))
                .doesNotContain(outsideX);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(outsideX, noncreature);
    }

    @Test
    @DisplayName("A library shorter than X reveals all remaining cards")
    void revealsShortLibrary() {
        Card firstCreature = new BodyOfJukai();
        Card noncreature = new SickeningShoal();
        Card secondCreature = new ForkedBranchGarami();
        harness.setLibrary(player1, List.of(firstCreature, noncreature, secondCreature));

        harness.setHand(player1, List.of(new EnshrinedMemories()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstCreature, secondCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(noncreature);
    }

    @Test
    @DisplayName("With X equal to zero, the library is unchanged")
    void zeroXRevealsNoCards() {
        Card topCreature = new BodyOfJukai();
        Card topNoncreature = new SickeningShoal();
        harness.setLibrary(player1, List.of(topCreature, topNoncreature));

        harness.setHand(player1, List.of(new EnshrinedMemories()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(topCreature, topNoncreature);
        assertThat(gd.playerHands.get(player1.getId()))
                .doesNotContain(topCreature, topNoncreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
