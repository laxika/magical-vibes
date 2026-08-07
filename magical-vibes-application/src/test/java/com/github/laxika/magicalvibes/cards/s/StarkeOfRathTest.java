package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StarkeOfRathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opponent's creature and hands Starke to that opponent")
    void destroysOpponentCreatureAndChangesControl() {
        Permanent starke = addReadyStarke(player1);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        activate(starke, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(starke.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(starke.getId()));
    }

    @Test
    @DisplayName("Destroys an artifact and hands Starke to the artifact's controller")
    void destroysArtifact() {
        Permanent starke = addReadyStarke(player1);
        harness.addToBattlefield(player2, new Spellbook());
        UUID artifactId = harness.getPermanentId(player2, "Spellbook");

        activate(starke, artifactId);

        harness.assertInGraveyard(player2, "Spellbook");
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(starke.getId()));
    }

    @Test
    @DisplayName("Targeting your own creature keeps Starke under your control")
    void targetingOwnCreatureKeepsControl() {
        Permanent starke = addReadyStarke(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        activate(starke, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(starke.getId()));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent starke = addReadyStarke(player1);
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(starke);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyStarke(Player player) {
        return addCreatureReady(player, new StarkeOfRath());
    }

    private void activate(Permanent starke, UUID targetId) {
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(starke);
        harness.activateAbility(player1, idx, null, targetId);
        harness.passBothPriorities();
    }
}
