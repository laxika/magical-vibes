package com.github.laxika.magicalvibes.cards.g;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GrislySpectacleTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonartifact creature and mills its controller by its power")
    void destroysCreatureAndMillsItsControllerByPower() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        UUID targetId = harness.getPermanentId(player2, "Elite Vanguard");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new IronMyr());
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Iron Myr");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonartifact creature");
    }

    @Test
    @DisplayName("Fizzles without milling when the target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        UUID targetId = harness.getPermanentId(player2, "Elite Vanguard");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
