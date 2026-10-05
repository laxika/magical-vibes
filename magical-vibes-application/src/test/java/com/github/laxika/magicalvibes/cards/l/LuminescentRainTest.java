package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MoongloveChangeling;
import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuminescentRain.class, BallyrushBanneret.class, MoongloveChangeling.class, Bitterblossom.class})
class LuminescentRainTest extends BaseCardTest {

    private int life(Player player) {
        return harness.getGameData().playerLifeTotals.get(player.getId());
    }

    private void payAndCast(Player player) {
        harness.castFromHand(player, new LuminescentRain(), "{2}{G}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains 2 life for each permanent of the chosen type you control")
    void gainsTwoLifePerChosenTypeCount() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        harness.addToBattlefield(player1, new BallyrushBanneret());
        int before = life(player1);

        payAndCast(player1);
        harness.handleListChoice(player1, "KITHKIN");

        assertThat(life(player1)).isEqualTo(before + 4);
    }

    @Test
    @DisplayName("Choosing a type you control none of gains no life")
    void chosenTypeYouControlNoneGainsZero() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        int before = life(player1);

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(life(player1)).isEqualTo(before);
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new MoongloveChangeling());
        int before = life(player1);

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(life(player1)).isEqualTo(before + 2);
    }

    @Test
    @DisplayName("Only the caster's permanents of the chosen type are counted")
    void onlyControllerPermanentsCounted() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        harness.addToBattlefield(player2, new BallyrushBanneret());
        harness.addToBattlefield(player2, new BallyrushBanneret());
        int before = life(player1);

        payAndCast(player1);
        harness.handleListChoice(player1, "KITHKIN");

        assertThat(life(player1)).isEqualTo(before + 2);
    }

    @Test
    @DisplayName("Noncreature kindred permanents count alongside changelings")
    void countsNoncreatureKindredPermanents() {
        harness.addToBattlefield(player1, new Bitterblossom());
        harness.addToBattlefield(player1, new MoongloveChangeling());
        harness.addToBattlefield(player1, new BallyrushBanneret());
        harness.addToBattlefield(player2, new Bitterblossom());
        int before = life(player1);

        payAndCast(player1);
        assertThat(life(player1)).isEqualTo(before);
        harness.handleListChoice(player1, "FAERIE");

        assertThat(life(player1)).isEqualTo(before + 4);
    }

    @Test
    @DisplayName("Each spell makes a fresh creature type choice")
    void consecutiveSpellsChooseIndependently() {
        harness.addToBattlefield(player1, new BallyrushBanneret());
        int before = life(player1);

        payAndCast(player1);
        harness.handleListChoice(player1, "SOLDIER");
        assertThat(life(player1)).isEqualTo(before + 2);

        payAndCast(player1);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(life(player1)).isEqualTo(before + 2);
    }
}
