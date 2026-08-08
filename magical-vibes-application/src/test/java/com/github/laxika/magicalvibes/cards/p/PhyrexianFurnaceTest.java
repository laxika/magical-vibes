package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianFurnaceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the bottom card of the target player's graveyard")
    void exilesBottomCardOfTargetPlayersGraveyard() {
        Card bottomCard = new GrizzlyBears();
        Card topCard = new Shock();
        harness.addToBattlefield(player1, new PhyrexianFurnace());
        harness.setGraveyard(player2, List.of(bottomCard, topCard));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bottomCard);
    }

    @Test
    @DisplayName("The bottom-card ability is a no-op for an empty graveyard")
    void bottomCardAbilityDoesNothingForEmptyGraveyard() {
        harness.addToBattlefield(player1, new PhyrexianFurnace());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice ability exiles its target and draws a card")
    void sacrificeAbilityExilesTargetAndDraws() {
        Card targetCard = new GrizzlyBears();
        Card drawCard = new Shock();
        Permanent furnace = harness.addToBattlefieldAndReturn(player1, new PhyrexianFurnace());
        harness.setGraveyard(player2, List.of(targetCard));
        harness.setLibrary(player1, List.of(drawCard));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, targetCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(furnace);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(furnace.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(targetCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawCard);
    }

    @Test
    @DisplayName("Sacrifice ability rejects a card that is not in a graveyard")
    void sacrificeAbilityRejectsMissingTarget() {
        harness.addToBattlefield(player1, new PhyrexianFurnace());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Card targetCard = new GrizzlyBears();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, targetCard.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
