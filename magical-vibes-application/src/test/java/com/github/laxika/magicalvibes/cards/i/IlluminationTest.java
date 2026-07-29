package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IlluminationTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact spell and its controller gains life equal to its mana value")
    void countersArtifactSpell() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, millstone.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Millstone");
        harness.assertNotOnBattlefield(player1, "Millstone");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Counters an enchantment spell and its controller gains life equal to its mana value")
    void countersEnchantmentSpell() {
        AngelicChorus chorus = new AngelicChorus();
        harness.setHand(player1, List.of(chorus));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, chorus.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Caster of Illumination gains no life")
    void casterGainsNoLife() {
        Millstone millstone = new Millstone();
        harness.setHand(player1, List.of(millstone));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Illumination()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.setLife(player2, 20);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, millstone.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }
}
