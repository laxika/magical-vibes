package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DwarvenStrikeForceTest extends BaseCardTest {

    @Test
    @DisplayName("Ability discards a card at random and grants first strike and haste")
    void discardsAndGrantsKeywords() {
        harness.addToBattlefield(player1, new DwarvenStrikeForce());
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, battlefieldIndex(player1, "Dwarven Strike Force"), null, null);
        harness.passBothPriorities();

        Permanent force = findPermanent(player1, "Dwarven Strike Force");
        assertThat(gqs.hasKeyword(gd, force, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, force, Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DwarvenStrikeForce());
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, battlefieldIndex(player1, "Dwarven Strike Force"), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent force = findPermanent(player1, "Dwarven Strike Force");
        assertThat(gqs.hasKeyword(gd, force, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, force, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new DwarvenStrikeForce());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, "Dwarven Strike Force"), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player player, String cardName) {
        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(cardName)) {
                return i;
            }
        }
        throw new IllegalStateException("Permanent not found: " + cardName);
    }
}
