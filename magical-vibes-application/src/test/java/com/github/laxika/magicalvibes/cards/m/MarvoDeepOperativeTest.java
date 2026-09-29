package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WorldspineWurm;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarvoDeepOperative.class, Forest.class, GrizzlyBears.class, WorldspineWurm.class})
class MarvoDeepOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking and winning a clash draws and offers a spell with mana value eight or less")
    void winningClashDrawsAndOffersFreeCast() {
        addCreatureReady(player1, new MarvoDeepOperative());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A spell with mana value greater than eight is not offered after a won clash")
    void highManaValueSpellIsNotOffered() {
        addCreatureReady(player1, new MarvoDeepOperative());
        harness.setLibrary(player1, List.of(new WorldspineWurm()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(WorldspineWurm.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the clash does not draw or offer a free cast")
    void losingClashDoesNothing() {
        addCreatureReady(player1, new MarvoDeepOperative());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
