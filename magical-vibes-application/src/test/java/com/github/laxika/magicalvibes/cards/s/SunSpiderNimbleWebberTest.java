package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicGift;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunSpiderNimbleWebber.class, AngelicGift.class, Bonesplitter.class})
class SunSpiderNimbleWebberTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying during its controller's turn only")
    void flyingOnlyDuringControllerTurn() {
        Permanent sunSpider = harness.addToBattlefieldAndReturn(player1, new SunSpiderNimbleWebber());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, sunSpider, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, sunSpider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Enters by searching for an Aura or Equipment card")
    void entersSearchesForAuraOrEquipment() {
        harness.setLibrary(player1, List.of(new AngelicGift(), new Bonesplitter()));
        harness.setHand(player1, List.of(new SunSpiderNimbleWebber()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards())
                .anyMatch(card -> card.getSubtypes().contains(CardSubtype.AURA));
        assertThat(search.params().cards())
                .anyMatch(card -> card.getSubtypes().contains(CardSubtype.EQUIPMENT));

        int auraIndex = 0;
        for (int i = 0; i < search.params().cards().size(); i++) {
            Card card = search.params().cards().get(i);
            if (card.getSubtypes().contains(CardSubtype.AURA)) {
                auraIndex = i;
                break;
            }
        }
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(auraIndex));

        harness.assertInHand(player1, "Angelic Gift");
    }
}
