package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LuminescentRainTest extends BaseCardTest {

    private int life(Player player) {
        return harness.getGameData().playerLifeTotals.get(player.getId());
    }

    private void payAndCast(Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.setHand(player, List.of(new LuminescentRain()));
        harness.castInstant(player, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains 2 life for each permanent of the chosen type you control")
    void gainsTwoLifePerChosenTypeCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int before = life(player1);

        payAndCast(player1);
        harness.handleListChoice(player1, "BEAR");

        assertThat(life(player1)).isEqualTo(before + 4);
    }

    @Test
    @DisplayName("Choosing a type you control none of gains no life")
    void chosenTypeYouControlNoneGainsZero() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        int before = life(player1);

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(life(player1)).isEqualTo(before);
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new AvianChangeling());
        int before = life(player1);

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(life(player1)).isEqualTo(before + 2);
    }

    @Test
    @DisplayName("Only the caster's permanents of the chosen type are counted")
    void onlyControllerPermanentsCounted() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int before = life(player1);

        payAndCast(player1);
        harness.handleListChoice(player1, "BEAR");

        assertThat(life(player1)).isEqualTo(before + 2);
    }
}
