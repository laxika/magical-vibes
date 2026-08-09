package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DryadGreenseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Offers a top land for optional reveal and puts it into hand when accepted")
    void acceptsTopLand() {
        addReadyDryad();
        Card topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand, new GrizzlyBears()));

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topLand);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topLand);
    }

    @Test
    @DisplayName("Declining the top land leaves it on top of the library")
    void declinesTopLand() {
        addReadyDryad();
        Card topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand, new GrizzlyBears()));

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topLand);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topLand);
    }

    @Test
    @DisplayName("A nonland top card stays on top without offering a choice")
    void nonlandTopCardStaysOnTop() {
        addReadyDryad();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest()));

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    private void addReadyDryad() {
        Permanent dryad = new Permanent(new DryadGreenseeker());
        dryad.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(dryad);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activateAbility() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
