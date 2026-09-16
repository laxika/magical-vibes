package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MazeOfShadows;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WasteLandPlaytest.class, MazeOfShadows.class, Forest.class})
class WasteLandPlaytestTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new WasteLandPlaytest());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Destroys target nonbasic land and gives its controller a Wastes token")
    void destroysTargetAndCreatesWastesForLandController() {
        harness.addToBattlefield(player1, new WasteLandPlaytest());
        harness.addToBattlefield(player2, new MazeOfShadows());
        UUID targetId = harness.getPermanentId(player2, "Maze of Shadows");

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Waste Land");
        harness.assertInGraveyard(player2, "Maze of Shadows");
        assertThat(findPermanents(player1, "Wastes")).isEmpty();

        Permanent wastes = findPermanents(player2, "Wastes").getFirst();
        assertThat(wastes.getCard().isToken()).isTrue();
        assertThat(wastes.getCard().hasType(CardType.LAND)).isTrue();

        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS))
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player1, new WasteLandPlaytest());
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
