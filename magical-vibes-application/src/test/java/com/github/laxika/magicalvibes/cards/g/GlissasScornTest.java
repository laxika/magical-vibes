package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.p.PristineTalisman;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.cards.s.Spellskite;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlissasScorn.class, PristineTalisman.class, DarksteelRelic.class, GlistenerElf.class, Spellskite.class})
class GlissasScornTest extends BaseCardTest {
    @Test
    @DisplayName("Resolving Glissa's Scorn destroys target artifact and its controller loses 1 life")
    void destroysArtifactAndControllerLosesLife() {
        harness.addToBattlefield(player2, new PristineTalisman());
        harness.setHand(player1, List.of(new GlissasScorn()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Pristine Talisman");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Pristine Talisman");
        harness.assertInGraveyard(player2, "Pristine Talisman");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Controller loses life even when artifact is indestructible")
    void controllerLosesLifeEvenWhenIndestructible() {
        harness.addToBattlefield(player2, new DarksteelRelic());
        harness.setHand(player1, List.of(new GlissasScorn()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Darksteel Relic");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        // Darksteel Relic is indestructible, should still be on battlefield
        harness.assertOnBattlefield(player2, "Darksteel Relic");
        // Controller still loses 1 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature with Glissa's Scorn")
    void cannotTargetNonartifactCreature() {
        harness.addToBattlefield(player2, new GlistenerElf());
        harness.setHand(player1, List.of(new GlissasScorn()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID creatureId = harness.getPermanentId(player2, "Glistener Elf");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new PristineTalisman());
        harness.setHand(player1, List.of(new GlissasScorn()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Pristine Talisman");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // No life loss when spell fizzles
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Destroying own artifact causes self to lose life")
    void destroyingOwnArtifactCausesSelfLifeLoss() {
        harness.addToBattlefield(player1, new PristineTalisman());
        harness.setHand(player1, List.of(new GlissasScorn()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        UUID targetId = harness.getPermanentId(player1, "Pristine Talisman");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Pristine Talisman");
        // Caster is also the artifact's controller, so they lose 1 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Artifact creatures are legal targets and only their controller loses life")
    void destroysArtifactCreature() {
        harness.addToBattlefield(player2, new Spellskite());
        harness.setHand(player1, List.of(new GlissasScorn()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Spellskite"));

        harness.assertNotOnBattlefield(player2, "Spellskite");
        harness.assertInGraveyard(player2, "Spellskite");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
