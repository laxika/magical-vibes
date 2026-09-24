package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotHulk.class, ZombieGoliath.class, GrizzlyBears.class})
class RotHulkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to the number of opponents' target Zombie cards")
    void returnsUpToNumberOfOpponentsTargetZombieCards() {
        Card firstZombie = new ZombieGoliath();
        Card secondZombie = new ZombieGoliath();
        Card nonZombie = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstZombie, secondZombie, nonZombie));
        harness.setHand(player1, List.of(new RotHulk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactly(firstZombie.getId(), secondZombie.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstZombie.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zombie Goliath");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondZombie, nonZombie);
    }

    @Test
    @DisplayName("ETB does not target non-Zombie cards")
    void doesNotTargetNonZombieCards() {
        Card nonZombie = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonZombie));
        harness.setHand(player1, List.of(new RotHulk()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
