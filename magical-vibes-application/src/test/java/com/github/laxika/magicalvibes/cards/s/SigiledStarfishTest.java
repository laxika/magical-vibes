package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SigiledStarfishTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability costs no mana, taps the Starfish and uses the stack")
    void activatingTapsAndUsesStack() {
        Permanent starfish = addReadyStarfish();

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(starfish.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability scries exactly one card")
    void resolvingScriesOne() {
        addReadyStarfish();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Keeping the scried card leaves it on top")
    void scryKeepOnTop() {
        addReadyStarfish();
        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top = deck.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(deck.getFirst()).isSameAs(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bottoming the scried card moves it to the bottom of the library")
    void scryBottom() {
        addReadyStarfish();
        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top = deck.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getFirst()).isNotSameAs(top);
        assertThat(deck.getLast()).isSameAs(top);
    }

    @Test
    @DisplayName("A summoning-sick Starfish cannot activate its tap ability")
    void summoningSickCannotActivate() {
        harness.addToBattlefieldAndReturn(player1, new SigiledStarfish()).setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyStarfish() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new SigiledStarfish());
        perm.setSummoningSick(false);
        return perm;
    }
}
