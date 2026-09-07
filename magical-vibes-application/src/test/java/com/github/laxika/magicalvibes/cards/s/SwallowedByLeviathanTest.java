package com.github.laxika.magicalvibes.cards.s;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwallowedByLeviathan.class, GrizzlyBears.class})
class SwallowedByLeviathanTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils before counting the graveyard for the ransom")
    void surveilsBeforeCountingGraveyardForRansom() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 4);

        Card kept = new GrizzlyBears();
        Card surveilled = new GrizzlyBears();
        harness.setHand(player2, List.of(new SwallowedByLeviathan()));
        harness.setLibrary(player2, List.of(kept, surveilled));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(surveilled);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Swallowed by Leviathan");
    }

    @Test
    @DisplayName("Counters the spell when its controller declines the ransom")
    void countersWhenControllerDeclinesRansom() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        GrizzlyBears target = new GrizzlyBears();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 5);

        Card surveilledOne = new GrizzlyBears();
        Card surveilledTwo = new GrizzlyBears();
        harness.setHand(player2, List.of(new SwallowedByLeviathan()));
        harness.setLibrary(player2, List.of(surveilledOne, surveilledTwo));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(surveilledOne, surveilledTwo);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target only a spell")
    void canTargetOnlySpell() {
        Card permanent = new GrizzlyBears();
        harness.addToBattlefield(player1, permanent);
        harness.setHand(player2, List.of(new SwallowedByLeviathan()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
