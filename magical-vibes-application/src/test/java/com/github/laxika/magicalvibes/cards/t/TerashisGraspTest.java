package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BakuAltar;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.i.InTheWebOfWar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerashisGrasp.class, BakuAltar.class, InTheWebOfWar.class, GnarledMass.class})
class TerashisGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and the caster gains life equal to its mana value")
    void destroysArtifactAndCasterGainsLife() {
        harness.addToBattlefield(player2, new BakuAltar());
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        int casterLifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Baku Altar");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Baku Altar");
        // Baku Altar has mana value 2.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(casterLifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Destroys target enchantment and the caster gains life equal to its mana value")
    void destroysEnchantmentAndCasterGainsLife() {
        harness.addToBattlefield(player2, new InTheWebOfWar());
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        int casterLifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player2, "In the Web of War");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "In the Web of War");
        // In the Web of War costs {3}{R}{R}.
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(casterLifeBefore + 5);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GnarledMass());
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID creatureId = harness.getPermanentId(player2, "Gnarled Mass");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
