package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinedMegalodon.class, GrizzlyBears.class, Shock.class})
class SpinedMegalodonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Spined Megalodon triggers scry 1")
    void attackingTriggersScryOne() {
        addCreatureReady(player1, new SpinedMegalodon());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void scryCanKeepTopCard() {
        addCreatureReady(player1, new SpinedMegalodon());
        SpinedMegalodon top = new SpinedMegalodon();
        SpinedMegalodon second = new SpinedMegalodon();
        harness.setLibrary(player1, List.of(top, second));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void attackTriggerLetsControllerPutTopCardOnBottom() {
        addCreatureReady(player2, new SpinedMegalodon());
        SpinedMegalodon top = new SpinedMegalodon();
        SpinedMegalodon second = new SpinedMegalodon();
        SpinedMegalodon opposingTop = new SpinedMegalodon();
        harness.setLibrary(player2, List.of(top, second));
        harness.setLibrary(player1, List.of(opposingTop));
        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opposingTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void attackingWithEmptyLibraryResolvesWithoutPrompt() {
        addCreatureReady(player1, new SpinedMegalodon());
        harness.setLibrary(player1, List.of());
        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentCannotTargetMegalodon() {
        Permanent megalodon = addCreatureReady(player1, new SpinedMegalodon());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, megalodon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerCanTargetMegalodon() {
        Permanent megalodon = addCreatureReady(player1, new SpinedMegalodon());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, megalodon.getId());
        assertThat(megalodon.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Spined Megalodon");
    }
}
