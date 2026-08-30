package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ConsulateDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({FromFatherToSon.class, ConsulateDreadnought.class, GrizzlyBears.class})
class FromFatherToSonTest extends BaseCardTest {

    @Test
    @DisplayName("A normal cast puts a Vehicle into hand")
    void normalCastPutsVehicleIntoHand() {
        Card vehicle = new ConsulateDreadnought();
        harness.setHand(player1, List.of(new FromFatherToSon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), vehicle));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(vehicle);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(vehicle);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().equals(vehicle));
    }

    @Test
    @DisplayName("Flashback puts a Vehicle onto the battlefield")
    void flashbackPutsVehicleOntoBattlefield() {
        Card vehicle = new ConsulateDreadnought();
        harness.setGraveyard(player1, List.of(new FromFatherToSon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), vehicle));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(vehicle);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().equals(vehicle));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(vehicle);
    }
}
