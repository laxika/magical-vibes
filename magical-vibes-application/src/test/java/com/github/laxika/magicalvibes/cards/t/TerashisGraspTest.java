package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TerashisGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and the caster gains life equal to its mana value")
    void destroysArtifactAndCasterGainsLife() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        int casterLifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Rod of Ruin");
        // Rod of Ruin has mana value 4
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(casterLifeBefore + 4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Destroys target enchantment and the caster gains life equal to its mana value")
    void destroysEnchantmentAndCasterGainsLife() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        int casterLifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Chorus");
        // Angelic Chorus costs {3}{W}{W}
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(casterLifeBefore + 5);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
