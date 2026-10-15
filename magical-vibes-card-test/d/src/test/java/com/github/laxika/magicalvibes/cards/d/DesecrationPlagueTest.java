package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChariotOfVictory;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxFleeceRam;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesecrationPlague.class, FontOfFertility.class, Forest.class, GrizzlyBears.class,
        NyxFleeceRam.class, ChariotOfVictory.class})
class DesecrationPlagueTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesDestroyEnchantment() {
        harness.addToBattlefield(player2, new FontOfFertility());
        harness.setHand(player1, List.of(new DesecrationPlague()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Font of Fertility");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Font of Fertility");
        harness.assertInGraveyard(player2, "Font of Fertility");
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvesDestroyLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new DesecrationPlague()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DesecrationPlague()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy an enchantment creature")
    void destroysEnchantmentCreature() {
        harness.addToBattlefield(player2, new NyxFleeceRam());
        harness.setHand(player1, List.of(new DesecrationPlague()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Nyx-Fleece Ram"));

        harness.assertNotOnBattlefield(player2, "Nyx-Fleece Ram");
        harness.assertInGraveyard(player2, "Nyx-Fleece Ram");
    }

    @Test
    @DisplayName("Can destroy the caster's own land")
    void destroysOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new DesecrationPlague()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Forest"));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new DesecrationPlague()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonenchantment artifact")
    void cannotTargetArtifact() {
        harness.addToBattlefield(player2, new ChariotOfVictory());
        harness.setHand(player1, List.of(new DesecrationPlague()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID artifactId = harness.getPermanentId(player2, "Chariot of Victory");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }
}
