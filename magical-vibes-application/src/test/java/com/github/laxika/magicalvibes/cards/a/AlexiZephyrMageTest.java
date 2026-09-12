package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChimericIdol;
import com.github.laxika.magicalvibes.cards.h.HazyHomunculus;
import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlexiZephyrMage.class, HazyHomunculus.class, PygmyRazorback.class, ChimericIdol.class})
class AlexiZephyrMageTest extends BaseCardTest {

    @Test
    void returnsXTargetCreaturesAndDiscardsTwoCards() {
        Permanent mage = addReadyMage();
        Permanent first = addCreatureReady(player2, new HazyHomunculus());
        Permanent second = addCreatureReady(player2, new PygmyRazorback());
        harness.setHand(player1, List.of(new PygmyRazorback(), new PygmyRazorback()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hazy Homunculus");
        harness.assertNotOnBattlefield(player2, "Pygmy Razorback");
        harness.assertInHand(player2, "Hazy Homunculus");
        harness.assertInHand(player2, "Pygmy Razorback");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Pygmy Razorback", "Pygmy Razorback");
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    void returnsAControlledCreatureToItsOwnersHand() {
        addReadyMage();
        Permanent target = addCreatureReady(player1, new PygmyRazorback());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new PygmyRazorback(), new PygmyRazorback()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 1, List.of(target.getId()));
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pygmy Razorback");
        harness.assertInHand(player2, "Pygmy Razorback");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotChooseMoreTargetsThanX() {
        addReadyMage();
        Permanent first = addCreatureReady(player2, new HazyHomunculus());
        Permanent second = addCreatureReady(player2, new PygmyRazorback());
        harness.setHand(player1, List.of(new PygmyRazorback(), new PygmyRazorback()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseFewerTargetsThanXWhenEnoughCreaturesAreAvailable() {
        addReadyMage();
        Permanent first = addCreatureReady(player2, new HazyHomunculus());
        addCreatureReady(player2, new PygmyRazorback());
        harness.setHand(player1, List.of(new PygmyRazorback(), new PygmyRazorback()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 2, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addReadyMage();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ChimericIdol());
        harness.setHand(player1, List.of(new PygmyRazorback(), new PygmyRazorback()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateWithZeroTargets() {
        Permanent mage = addReadyMage();
        harness.setHand(player1, List.of(new PygmyRazorback(), new PygmyRazorback()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Pygmy Razorback", "Pygmy Razorback");
    }

    @Test
    void cannotActivateWithoutTwoCardsToDiscard() {
        addReadyMage();
        harness.setHand(player1, List.of(new PygmyRazorback()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMage() {
        return addCreatureReady(player1, new AlexiZephyrMage());
    }
}
