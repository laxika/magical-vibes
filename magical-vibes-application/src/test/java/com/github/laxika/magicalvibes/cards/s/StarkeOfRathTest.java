package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.f.FoolsTome;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarkeOfRath.class, CanopySpider.class, FoolsTome.class, Forest.class})
class StarkeOfRathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an opponent's creature and hands Starke to that opponent")
    void destroysOpponentCreatureAndChangesControl() {
        Permanent starke = addReadyStarke(player1);
        Permanent spider = addCreatureReady(player2, new CanopySpider());

        activate(starke, spider.getId());

        harness.assertInGraveyard(player2, "Canopy Spider");
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(starke.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(starke.getId()));
    }

    @Test
    @DisplayName("Destroys an artifact and hands Starke to the artifact's controller")
    void destroysArtifact() {
        Permanent starke = addReadyStarke(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FoolsTome());

        activate(starke, artifact.getId());

        harness.assertInGraveyard(player2, "Fool's Tome");
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(starke.getId()));
    }

    @Test
    @DisplayName("Targeting your own creature keeps Starke under your control")
    void targetingOwnCreatureKeepsControl() {
        Permanent starke = addReadyStarke(player1);
        Permanent spider = addCreatureReady(player1, new CanopySpider());

        activate(starke, spider.getId());

        harness.assertInGraveyard(player1, "Canopy Spider");
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(starke.getId()));
    }

    @Test
    @DisplayName("Fizzles when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        Permanent starke = addReadyStarke(player1);
        Permanent spider = addCreatureReady(player2, new CanopySpider());

        activateWithoutResolving(starke, spider.getId());
        gd.playerBattlefields.get(player2.getId()).remove(spider);

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertOnBattlefield(player1, "Starke of Rath");
        harness.assertNotOnBattlefield(player2, "Starke of Rath");
    }

    @Test
    @DisplayName("Still destroys the target if Starke leaves before resolution")
    void destroysTargetIfSourceLeavesBeforeResolution() {
        Permanent starke = addReadyStarke(player1);
        Permanent spider = addCreatureReady(player2, new CanopySpider());

        activateWithoutResolving(starke, spider.getId());
        gd.playerBattlefields.get(player1.getId()).remove(starke);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Canopy Spider");
        harness.assertNotOnBattlefield(player1, "Starke of Rath");
        harness.assertNotOnBattlefield(player2, "Starke of Rath");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent starke = addReadyStarke(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(starke);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyStarke(Player player) {
        return addCreatureReady(player, new StarkeOfRath());
    }

    private void activate(Permanent starke, UUID targetId) {
        activateWithoutResolving(starke, targetId);
        harness.passBothPriorities();
    }

    private void activateWithoutResolving(Permanent starke, UUID targetId) {
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(starke);
        harness.activateAbility(player1, idx, null, targetId);
    }
}
