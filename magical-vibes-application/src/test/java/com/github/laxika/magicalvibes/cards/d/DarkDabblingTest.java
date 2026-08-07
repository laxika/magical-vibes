package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DarkDabblingTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerates the target creature and draws a card")
    void regeneratesTargetAndDraws() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(permanentOf(player1, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can regenerate a creature an opponent controls")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        cast(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(permanentOf(player2, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Without spell mastery only the target is regenerated")
    void withoutSpellMasteryOnlyTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(permanentOf(player1, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
        assertThat(permanentOf(player1, "Runeclaw Bear").getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Spell mastery also regenerates each other creature you control, but never twice the target")
    void spellMasteryRegeneratesOtherOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(permanentOf(player1, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
        assertThat(permanentOf(player1, "Runeclaw Bear").getRegenerationShield()).isEqualTo(1);
        assertThat(permanentOf(player2, "Grizzly Bears").getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Spell mastery regenerates all your creatures when the target is an opponent's creature")
    void spellMasteryWithOpponentTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        cast(player1, harness.getPermanentId(player2, "Runeclaw Bear"));

        assertThat(permanentOf(player1, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
        assertThat(permanentOf(player2, "Runeclaw Bear").getRegenerationShield()).isEqualTo(1);
    }

    private void cast(Player player, UUID targetId) {
        harness.setHand(player, List.of(new DarkDabbling()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent permanentOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
