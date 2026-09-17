package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenStrikeForce.class, Forest.class})
class DwarvenStrikeForceTest extends BaseCardTest {

    @Test
    @DisplayName("Ability discards a card at random and grants first strike and haste")
    void discardsAndGrantsKeywords() {
        harness.addToBattlefield(player1, new DwarvenStrikeForce());
        harness.setHand(player1, List.of(new Forest()));
        Permanent force = findPermanent(player1, "Dwarven Strike Force");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(force), null, null);
        harness.passBothPriorities();

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
        Permanent force = findPermanent(player1, "Dwarven Strike Force");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(force), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, force, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, force, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Ability grants keywords only to the creature it is activated from")
    void onlySourceCreatureGainsKeywords() {
        harness.addToBattlefield(player1, new DwarvenStrikeForce());
        harness.addToBattlefield(player1, new DwarvenStrikeForce());
        harness.setHand(player1, List.of(new Forest()));

        List<Permanent> forces = findPermanents(player1, "Dwarven Strike Force");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forces.get(0)), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, forces.get(0), Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forces.get(0), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forces.get(1), Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, forces.get(1), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new DwarvenStrikeForce());
        harness.setHand(player1, List.of());
        Permanent force = findPermanent(player1, "Dwarven Strike Force");

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(force), null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
