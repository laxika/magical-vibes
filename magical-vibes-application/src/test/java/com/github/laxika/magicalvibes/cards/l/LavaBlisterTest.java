package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LavaBlisterTest extends BaseCardTest {

    @Test
    @DisplayName("The target land's controller takes 6 damage and the land survives")
    void targetControllerTakesDamageAndLandSurvives() {
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Ghost Quarter");
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("The target land's controller declines and the land is destroyed")
    void decliningDestroysLand() {
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Ghost Quarter");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target land leaves before resolution")
    void fizzlesIfTargetLeaves() {
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");
        harness.castSorcery(player1, 0, targetId);
        GameData gameData = harness.getGameData();
        gameData.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gameData.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }
}
