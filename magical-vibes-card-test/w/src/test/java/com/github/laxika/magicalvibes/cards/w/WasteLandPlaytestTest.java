package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
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

@CardUsed({WasteLandPlaytest.class, MazeOfShadows.class, Forest.class, DarksteelCitadel.class})
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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MazeOfShadows()).getId();

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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifices Waste Land as a cost before destroying the target")
    void sacrificesSourceBeforeResolution() {
        harness.addToBattlefield(player1, new WasteLandPlaytest());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MazeOfShadows()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);

        harness.assertInGraveyard(player1, "Waste Land");
        harness.assertNotOnBattlefield(player1, "Waste Land");
        harness.assertOnBattlefield(player2, "Maze of Shadows");
        assertThat(findPermanents(player2, "Wastes")).isEmpty();
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Maze of Shadows");
        assertThat(countPermanents(player2, "Wastes")).isEqualTo(1);
    }

    @Test
    @DisplayName("Wastes tokens are basic lands and cannot be targeted by Waste Land")
    void cannotTargetCreatedWastesToken() {
        harness.addToBattlefield(player1, new WasteLandPlaytest());
        harness.addToBattlefield(player1, new WasteLandPlaytest());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MazeOfShadows()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        UUID wastesId = findPermanent(player2, "Wastes").getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, wastesId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Waste Land");
        assertThat(countPermanents(player2, "Wastes")).isEqualTo(1);
    }

    @Test
    @DisplayName("Can destroy your own nonbasic land and create the token for you")
    void canTargetOwnLand() {
        harness.addToBattlefield(player1, new WasteLandPlaytest());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new MazeOfShadows()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Maze of Shadows");
        assertThat(countPermanents(player1, "Wastes")).isEqualTo(1);
        assertThat(findPermanents(player2, "Wastes")).isEmpty();
    }

    @Test
    @DisplayName("Creates a Wastes even when the target land is indestructible")
    void createsTokenForIndestructibleLandController() {
        harness.addToBattlefield(player1, new WasteLandPlaytest());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotInGraveyard(player2, "Darksteel Citadel");
        harness.assertInGraveyard(player1, "Waste Land");
        assertThat(countPermanents(player2, "Wastes")).isEqualTo(1);
        assertThat(findPermanents(player1, "Wastes")).isEmpty();
    }

    @Test
    @DisplayName("Does not create another token when the target is destroyed in response")
    void illegalTargetDoesNotCreateToken() {
        harness.addToBattlefield(player1, new WasteLandPlaytest());
        harness.addToBattlefield(player2, new WasteLandPlaytest());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MazeOfShadows()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.activateAbility(player2, 0, 1, null, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Waste Land");
        harness.assertInGraveyard(player2, "Waste Land");
        harness.assertInGraveyard(player2, "Maze of Shadows");
        assertThat(countPermanents(player2, "Wastes")).isEqualTo(1);
        assertThat(findPermanents(player1, "Wastes")).isEmpty();
    }
}
