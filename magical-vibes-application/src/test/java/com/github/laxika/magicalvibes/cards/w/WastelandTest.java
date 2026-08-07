package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WastelandTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new Wasteland());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Destroys target nonbasic land an opponent controls")
    void destroysOpponentNonbasicLand() {
        harness.addToBattlefield(player1, new Wasteland());
        harness.addToBattlefield(player2, new GhostQuarter());
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
        harness.assertInGraveyard(player2, "Ghost Quarter");
        harness.assertInGraveyard(player1, "Wasteland");
    }

    @Test
    @DisplayName("Can destroy a nonbasic land its own controller controls")
    void canDestroyOwnNonbasicLand() {
        harness.addToBattlefield(player1, new Wasteland());
        harness.addToBattlefield(player1, new GhostQuarter());
        UUID targetId = harness.getPermanentId(player1, "Ghost Quarter");

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghost Quarter");
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player1, new Wasteland());
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new Wasteland());
        harness.addToBattlefield(player2, new GhostQuarter());
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        harness.getGameData().playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
