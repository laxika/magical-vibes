package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BakuAltar;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.i.InTheWebOfWar;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerashisGrasp.class, BakuAltar.class, InTheWebOfWar.class, GnarledMass.class,
        ThatWhichWasTaken.class})
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
        harness.castAndResolveSorcery(player1, 0, targetId);

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
        harness.castAndResolveSorcery(player1, 0, targetId);

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

    @Test
    @DisplayName("Gains life even when an indestructible artifact cannot be destroyed")
    void gainsLifeFromIndestructibleArtifact() {
        harness.addToBattlefield(player2, new ThatWhichWasTaken());
        Permanent altar = harness.addToBattlefieldAndReturn(player2, new BakuAltar());
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, altar.getId());
        harness.passBothPriorities();

        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveSorcery(player1, 0, altar.getId());

        harness.assertOnBattlefield(player2, "Baku Altar");
        harness.assertNotInGraveyard(player2, "Baku Altar");
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not gain life when the target leaves before resolution")
    void doesNotGainLifeWhenTargetLeaves() {
        Permanent altar = harness.addToBattlefieldAndReturn(player2, new BakuAltar());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castSorcery(player1, 0, altar.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, altar));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Baku Altar");
        harness.assertInGraveyard(player1, "Terashi's Grasp");
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Can destroy the caster's own enchantment and gain life")
    void destroysOwnEnchantment() {
        harness.addToBattlefield(player1, new InTheWebOfWar());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new TerashisGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveSorcery(player1, 0,
                harness.getPermanentId(player1, "In the Web of War"));

        harness.assertInGraveyard(player1, "In the Web of War");
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }
}
