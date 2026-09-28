package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KeldonNecropolis;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TsabosWeb.class, KeldonNecropolis.class, Forest.class})
class TsabosWebTest extends BaseCardTest {

    @Test
    @DisplayName("Enters-the-battlefield ability draws a card")
    void entersTheBattlefieldDrawsACard() {
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new TsabosWeb(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A land with a non-mana activated ability does not untap")
    void landWithNonManaActivatedAbilityDoesNotUntap() {
        harness.addToBattlefield(player1, new TsabosWeb());
        Permanent keldonNecropolis = harness.addToBattlefieldAndReturn(player2, new KeldonNecropolis());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        keldonNecropolis.tap();
        forest.tap();

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UNTAP);

        assertThat(keldonNecropolis.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The restriction also applies to matching lands controlled by Tsabo's Web's controller")
    void ownLandWithNonManaActivatedAbilityDoesNotUntap() {
        harness.addToBattlefield(player1, new TsabosWeb());
        Permanent keldonNecropolis = harness.addToBattlefieldAndReturn(player1, new KeldonNecropolis());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        keldonNecropolis.tap();
        forest.tap();

        harness.performUntapStep(player1);

        assertThat(keldonNecropolis.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }
}
