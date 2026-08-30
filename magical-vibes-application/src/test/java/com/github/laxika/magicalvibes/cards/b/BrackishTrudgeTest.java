package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrackishTrudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new BrackishTrudge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent trudge = findPermanent(player1, "Brackish Trudge");
        assertThat(trudge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate its graveyard ability without life gain")
    void cannotActivateWithoutLifeGain() {
        harness.setGraveyard(player1, List.of(new BrackishTrudge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns itself from the graveyard after life gain")
    void returnsFromGraveyardAfterLifeGain() {
        BrackishTrudge trudge = new BrackishTrudge();
        harness.setGraveyard(player1, List.of(trudge));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(trudge);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(trudge);
    }
}
