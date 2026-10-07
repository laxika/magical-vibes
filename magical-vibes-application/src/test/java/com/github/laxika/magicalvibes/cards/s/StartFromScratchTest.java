package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PillardropWarden;
import com.github.laxika.magicalvibes.cards.p.ProfessorOnyx;
import com.github.laxika.magicalvibes.cards.t.TeamPennant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StartFromScratch.class, PillardropWarden.class, TeamPennant.class, ProfessorOnyx.class})
class StartFromScratchTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 1 damage to target player")
    void damageModeDealsDamageToPlayer() {
        harness.setHand(player1, List.of(new StartFromScratch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Start from Scratch");
    }

    @Test
    @DisplayName("Artifact mode destroys target artifact")
    void artifactModeDestroysArtifact() {
        harness.addToBattlefield(player2, new TeamPennant());
        harness.setHand(player1, List.of(new StartFromScratch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 1, harness.getPermanentId(player2, "Team Pennant"));

        harness.assertNotOnBattlefield(player2, "Team Pennant");
        harness.assertInGraveyard(player2, "Team Pennant");
    }

    @Test
    @DisplayName("Artifact mode cannot target a nonartifact permanent")
    void artifactModeCannotTargetNonartifact() {
        harness.addToBattlefield(player2, new PillardropWarden());
        harness.setHand(player1, List.of(new StartFromScratch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, 1, harness.getPermanentId(player2, "Pillardrop Warden")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Damage mode damages a creature without destroying it or other artifacts")
    void damageModeDealsOneDamageToCreature() {
        harness.addToBattlefield(player2, new PillardropWarden());
        harness.addToBattlefield(player2, new TeamPennant());
        harness.setHand(player1, List.of(new StartFromScratch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0,
                harness.getPermanentId(player2, "Pillardrop Warden"));

        harness.assertOnBattlefield(player2, "Pillardrop Warden");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Team Pennant");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage mode can target its controller")
    void damageModeCanTargetController() {
        harness.setHand(player1, List.of(new StartFromScratch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0, player1.getId());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Artifact mode can destroy its controller's artifact without dealing damage")
    void artifactModeCanTargetOwnArtifact() {
        harness.addToBattlefield(player1, new TeamPennant());
        harness.setHand(player1, List.of(new StartFromScratch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 1,
                harness.getPermanentId(player1, "Team Pennant"));

        harness.assertNotOnBattlefield(player1, "Team Pennant");
        harness.assertInGraveyard(player1, "Team Pennant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage mode removes one loyalty from a planeswalker")
    void damageModeCanTargetPlaneswalker() {
        var planeswalker = harness.enterBattlefieldAndReturn(player2, new ProfessorOnyx());
        harness.setHand(player1, List.of(new StartFromScratch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0,
                planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage mode cannot target a noncreature artifact")
    void damageModeCannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player2, new TeamPennant());
        harness.setHand(player1, List.of(new StartFromScratch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0,
                harness.getPermanentId(player2, "Team Pennant")))
                .isInstanceOf(IllegalStateException.class);
    }
}
