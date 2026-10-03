package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BuildersBlessing;
import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.s.SpectralPrison;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cursebreak.class, BuildersBlessing.class, AngelicWall.class, SpectralPrison.class})
class CursebreakTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target enchantment and gains 2 life")
    void destroysEnchantmentAndGainsLife() {
        harness.addToBattlefield(player2, new BuildersBlessing());
        harness.setHand(player1, List.of(new Cursebreak()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Builder's Blessing");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Builder's Blessing");
        harness.assertInGraveyard(player2, "Builder's Blessing");
        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Cursebreak");
    }

    @Test
    @DisplayName("Can destroy own enchantment")
    void canDestroyOwnEnchantment() {
        harness.addToBattlefield(player1, new BuildersBlessing());
        harness.setHand(player1, List.of(new Cursebreak()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Builder's Blessing");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Builder's Blessing");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Fizzles entirely if the target is gone, so no life is gained")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new BuildersBlessing());
        harness.setHand(player1, List.of(new Cursebreak()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Builder's Blessing");
        harness.castInstant(player1, 0, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Cursebreak");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new AngelicWall());
        harness.setHand(player1, List.of(new Cursebreak()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID creatureId = harness.getPermanentId(player2, "Angelic Wall");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Destroys an attached Aura without destroying its enchanted creature")
    void destroysAuraAndLeavesCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new AngelicWall()).getId();
        harness.setHand(player1, List.of(new SpectralPrison(), new Cursebreak()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creatureId);
        harness.passBothPriorities();

        UUID auraId = harness.getPermanentId(player1, "Spectral Prison");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, auraId);

        harness.assertNotOnBattlefield(player1, "Spectral Prison");
        harness.assertInGraveyard(player1, "Spectral Prison");
        harness.assertOnBattlefield(player2, "Angelic Wall");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot cast without an enchantment target just to gain life")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new Cursebreak()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Cursebreak");
    }
}
