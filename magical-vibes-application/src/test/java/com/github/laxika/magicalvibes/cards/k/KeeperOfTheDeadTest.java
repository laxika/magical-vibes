package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeeperOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target nonblack creature controlled by the targeted opponent")
    void destroysTargetNonblackCreature() {
        readyKeeper(List.of(new GrizzlyBears(), new GrizzlyBears(), new GiantGrowth()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(target.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .contains(target.getCard().getId());
    }

    @Test
    @DisplayName("Checks the graveyard difference only when activating")
    void graveyardDifferenceIsCheckedOnlyOnActivation() {
        readyKeeper(List.of(new GrizzlyBears(), new GrizzlyBears()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(target.getId());
    }

    @Test
    @DisplayName("Cannot activate when the targeted opponent has not fallen behind by two creature cards")
    void cannotActivateWithoutRequiredGraveyardDifference() {
        readyKeeper(List.of(new GrizzlyBears()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        readyKeeper(List.of(new GrizzlyBears(), new GrizzlyBears()), List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KeeperOfTheDead());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void readyKeeper(List<Card> controllerGraveyard, List<Card> opponentGraveyard) {
        harness.setGraveyard(player1, controllerGraveyard);
        harness.setGraveyard(player2, opponentGraveyard);
        addCreatureReady(player1, new KeeperOfTheDead());
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
