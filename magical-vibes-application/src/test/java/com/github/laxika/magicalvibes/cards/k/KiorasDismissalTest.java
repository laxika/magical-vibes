package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KiorasDismissalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns each target enchantment to its owner's hand")
    void returnsTargetEnchantmentsToTheirOwnersHands() {
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new AngelicChorus());

        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, List.of(ownEnchantment.getId(), opponentEnchantment.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertInHand(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Strive requires one additional blue mana per additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent firstEnchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent secondEnchantment = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());

        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstEnchantment.getId(), secondEnchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose no targets")
    void canChooseNoTargets() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Can target only enchantments")
    void cannotTargetNonEnchantment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KiorasDismissal()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }
}
