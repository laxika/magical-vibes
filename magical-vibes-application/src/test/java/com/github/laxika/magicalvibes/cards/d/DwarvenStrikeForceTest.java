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
        Permanent force = harness.addToBattlefieldAndReturn(player1, new DwarvenStrikeForce());
        harness.setHand(player1, List.of(new Forest()));

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
        Permanent force = harness.addToBattlefieldAndReturn(player1, new DwarvenStrikeForce());
        harness.setHand(player1, List.of(new Forest()));

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
        List<Permanent> forces = List.of(
                harness.addToBattlefieldAndReturn(player1, new DwarvenStrikeForce()),
                harness.addToBattlefieldAndReturn(player1, new DwarvenStrikeForce()));
        harness.setHand(player1, List.of(new Forest()));

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
        Permanent force = harness.addToBattlefieldAndReturn(player1, new DwarvenStrikeForce());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(force), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardIsPaidBeforeKeywordsAreGranted() {
        Permanent force = harness.addToBattlefieldAndReturn(player1, new DwarvenStrikeForce());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of(first, second));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst()).isIn(first, second);
        assertThat(gd.playerHands.get(player1.getId()))
                .doesNotContain(gd.playerGraveyards.get(player1.getId()).getFirst());
        assertThat(gqs.hasKeyword(gd, force, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, force, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, force, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, force, Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void tappedCreatureCanActivateRepeatedlyByPayingEachTime() {
        Permanent force = harness.addToBattlefieldAndReturn(player1, new DwarvenStrikeForce());
        force.tap();
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(force.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.hasKeyword(gd, force, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, force, Keyword.HASTE)).isTrue();
    }
}
