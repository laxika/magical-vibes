package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeaceAndQuietTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys two target enchantments")
    void destroysTwoTargetEnchantments() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new PeaceAndQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID firstTarget = harness.getPermanentId(player2, "Angelic Chorus");
        UUID secondTarget = harness.getPermanentId(player2, "Glorious Anthem");
        harness.castInstant(player1, 0, List.of(firstTarget, secondTarget));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Requires two different enchantment targets")
    void requiresTwoDifferentTargets() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new PeaceAndQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(targetId, targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PeaceAndQuiet()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(enchantmentId, creatureId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchantment");
    }
}
