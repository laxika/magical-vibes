package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OvereagerApprentice.class, Forest.class})
class OvereagerApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card and sacrificing Overeager Apprentice adds three black mana")
    void discardAndSacrificeAddsThreeBlackMana() {
        harness.addToBattlefield(player1, new OvereagerApprentice());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Overeager Apprentice");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefield(player1, new OvereagerApprentice());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Overeager Apprentice");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Apprentice can produce mana without using the stack")
    void tappedSummoningSickApprenticeCanActivate() {
        var apprentice = harness.addToBattlefieldAndReturn(player1, new OvereagerApprentice());
        apprentice.setSummoningSick(true);
        apprentice.tap();
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Overeager Apprentice");
        harness.assertInGraveyard(player1, "Overeager Apprentice");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("A creature card can be discarded and mana goes only to the activating player")
    void canDiscardCreatureCard() {
        harness.addToBattlefield(player2, new OvereagerApprentice());
        harness.setHand(player2, List.of(new OvereagerApprentice()));

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Overeager Apprentice");
    }
}
