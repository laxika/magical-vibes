package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CloudSpirit;
import com.github.laxika.magicalvibes.cards.f.FoulImp;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({Ransack.class, CloudSpirit.class, FoulImp.class, Shock.class, SpinedWurm.class})
class RansackTest extends BaseCardTest {

    private void castRansack() {
        harness.setHand(player1, List.of(new Ransack()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Controller splits and orders the target player's top five cards")
    void splitsAndOrdersTargetLibrary() {
        Card c0 = new CloudSpirit();
        Card c1 = new Shock();
        Card c2 = new FoulImp();
        Card c3 = new SpinedWurm();
        Card c4 = new Ransack();
        Card c5 = new Shock();
        harness.setLibrary(player2, List.of(c0, c1, c2, c3, c4, c5));

        castRansack();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.libraryOwnerId()).isEqualTo(player2.getId());
        assertThat(scry.cards()).containsExactly(c0, c1, c2, c3, c4);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(4, 1), List.of(3, 0, 2)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(c4, c1, c5, c3, c0, c2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short target library moves all available cards through the split")
    void handlesShortTargetLibrary() {
        Card c0 = new Shock();
        Card c1 = new FoulImp();
        harness.setLibrary(player2, List.of(c0, c1));

        castRansack();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(c0, c1);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(c1, c0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty target library resolves without opening a choice")
    void handlesEmptyTargetLibrary() {
        harness.setLibrary(player2, List.of());

        castRansack();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
