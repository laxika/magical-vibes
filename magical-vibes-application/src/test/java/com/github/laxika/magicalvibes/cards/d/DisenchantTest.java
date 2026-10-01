package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Disenchant.class, TormodsCrypt.class, Dodecapod.class, BadMoon.class,
        Squire.class, Desert.class})
class DisenchantTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target artifact")
    void resolvesAndDestroysArtifact() {
        harness.addToBattlefield(player2, new TormodsCrypt());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Tormod's Crypt");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Tormod's Crypt");
        harness.assertInGraveyard(player2, "Tormod's Crypt");
    }

    @Test
    @DisplayName("Can target an artifact controlled by the caster")
    void resolvesAndDestroysOwnArtifact() {
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Tormod's Crypt");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Tormod's Crypt");
        harness.assertInGraveyard(player1, "Tormod's Crypt");
    }

    @Test
    @DisplayName("Resolving destroys target artifact creature")
    void resolvesAndDestroysArtifactCreature() {
        harness.addToBattlefield(player2, new Dodecapod());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Dodecapod");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Dodecapod");
        harness.assertInGraveyard(player2, "Dodecapod");
    }

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesAndDestroysEnchantment() {
        harness.addToBattlefield(player2, new BadMoon());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Bad Moon");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Bad Moon");
        harness.assertInGraveyard(player2, "Bad Moon");
    }

    @Test
    @DisplayName("Cannot target creature with Disenchant")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new Squire());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Squire");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target land with Disenchant")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Desert());
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID landId = harness.getPermanentId(player2, "Desert");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }
}
