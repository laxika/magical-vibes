package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtisticRefusal.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class})
class ArtisticRefusalTest extends BaseCardTest {

    @Test
    void counterModeCountersTargetSpell() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new ArtisticRefusal()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0}),
                shock.getId(), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void drawModeDrawsTwoThenRequiresDiscard() {
        GrizzlyBears discard = new GrizzlyBears();
        harness.setHand(player1, List.of(new ArtisticRefusal(), discard));
        harness.setLibrary(player1, List.of(new Island(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.assertInHand(player1, "Island");
        harness.assertInHand(player1, "Forest");

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void bothModesCounterAndDrawThenDiscard() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new ArtisticRefusal(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 1}, shock.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInHand(player1, "Island");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void counterModeRejectsNonSpellTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        harness.setHand(player1, List.of(new ArtisticRefusal()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, new int[]{0}, bears.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
