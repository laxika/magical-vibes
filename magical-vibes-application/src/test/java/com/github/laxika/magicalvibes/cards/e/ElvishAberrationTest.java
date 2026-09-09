package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ElvishAberration.class, Forest.class, GrizzlyBears.class})
class ElvishAberrationTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Elvish Aberration adds three green mana")
    void tappingAddsThreeGreenMana() {
        harness.addToBattlefield(player1, new ElvishAberration());
        Permanent aberration = gd.playerBattlefields.get(player1.getId()).getFirst();
        aberration.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(aberration.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Forestcycling discards the card and offers only Forest cards")
    void forestcyclingDiscardsAndOffersForests() {
        harness.setHand(player1, List.of(new ElvishAberration()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Aberration");
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(2).allMatch(card -> card instanceof Forest);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
    }
}
