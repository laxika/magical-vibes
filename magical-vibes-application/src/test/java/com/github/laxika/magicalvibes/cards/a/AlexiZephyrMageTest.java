package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlexiZephyrMageTest extends BaseCardTest {

    @Test
    void returnsXTargetCreaturesAndDiscardsTwoCards() {
        Permanent mage = addReadyMage();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, 2, List.of(first.getId(), second.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Hill Giant");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest", "Forest");
        assertThat(mage.isTapped()).isTrue();
    }

    @Test
    void cannotChooseMoreTargetsThanX() {
        addReadyMage();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addReadyMage();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, 1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutTwoCardsToDiscard() {
        addReadyMage();
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMage() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new AlexiZephyrMage());
        mage.setSummoningSick(false);
        return mage;
    }
}
