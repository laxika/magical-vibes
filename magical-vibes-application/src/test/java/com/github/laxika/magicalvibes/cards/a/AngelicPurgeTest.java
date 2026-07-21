package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AngelicPurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a permanent and exiles target creature")
    void sacrificesPermanentAndExilesCreature() {
        Permanent sacrifice = new Permanent(new Forest());
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new AngelicPurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile target artifact")
    void canExileArtifact() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        Permanent target = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new AngelicPurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Spellbook"));
    }

    @Test
    @DisplayName("Can exile target enchantment")
    void canExileEnchantment() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        Permanent target = new Permanent(new AuraOfSilence());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new AngelicPurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aura of Silence");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Aura of Silence"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.setHand(player1, List.of(new AngelicPurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.castSorceryWithSacrifice(player1, 0, land.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast without a permanent to sacrifice")
    void cannotCastWithoutSacrifice() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new AngelicPurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
