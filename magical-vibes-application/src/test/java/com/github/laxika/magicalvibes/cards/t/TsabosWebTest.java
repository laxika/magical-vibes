package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StripMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TsabosWebTest extends BaseCardTest {

    @Test
    @DisplayName("Enters-the-battlefield ability draws a card")
    void entersTheBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new TsabosWeb()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A land with a non-mana activated ability does not untap")
    void landWithNonManaActivatedAbilityDoesNotUntap() {
        harness.addToBattlefield(player1, new TsabosWeb());
        Permanent stripMine = harness.addToBattlefieldAndReturn(player2, new StripMine());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        stripMine.tap();
        forest.tap();

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(stripMine.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }
}
