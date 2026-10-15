package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BrokenFall;
import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.d.DeathsPresence;
import com.github.laxika.magicalvibes.cards.f.FoolsTome;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WayfaringTemple;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarkeOfRath.class, CanopySpider.class, FoolsTome.class, Forest.class,
        BrokenFall.class, DeathsPresence.class, WayfaringTemple.class})
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

    @Test
    @DisplayName("Destroys the target before transferring control, preserving its last known power")
    void destructionPrecedesControlTransfer() {
        Permanent starke = addReadyStarke(player1);
        harness.addToBattlefield(player2, new DeathsPresence());
        Permanent temple = addCreatureReady(player2, new WayfaringTemple());
        Permanent spider = addCreatureReady(player2, new CanopySpider());

        activate(starke, temple.getId());

        harness.assertInGraveyard(player2, "Wayfaring Temple");
        harness.assertOnBattlefield(player2, "Starke of Rath");
        harness.handlePermanentChosen(player2, spider.getId());
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Starke can target and destroy himself")
    void canDestroySelf() {
        Permanent starke = addReadyStarke(player1);

        activate(starke, starke.getId());

        harness.assertInGraveyard(player1, "Starke of Rath");
        harness.assertNotOnBattlefield(player1, "Starke of Rath");
        harness.assertNotOnBattlefield(player2, "Starke of Rath");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent starke = harness.addToBattlefieldAndReturn(player1, new StarkeOfRath());
        Permanent spider = addCreatureReady(player2, new CanopySpider());
        starke.setSummoningSick(true);

        assertThatThrownBy(() -> activateWithoutResolving(starke, spider.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Canopy Spider");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The tap cost stays paid when control changes")
    void remainsTappedUnderNewController() {
        Permanent starke = addReadyStarke(player1);
        Permanent spider = addCreatureReady(player2, new CanopySpider());

        activate(starke, spider.getId());

        assertThat(starke.isTapped()).isTrue();
        assertThat(starke.isSummoningSick()).isTrue();
        harness.ensurePriority(player2);
        int idx = gd.playerBattlefields.get(player2.getId()).indexOf(starke);
        assertThatThrownBy(() -> harness.activateAbility(player2, idx, null, starke.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regenerating the target does not prevent the control transfer")
    void regeneratedTargetSurvivesAndControllerGainsStarke() {
        Permanent starke = addReadyStarke(player1);
        Permanent spider = addCreatureReady(player2, new CanopySpider());
        Permanent fall = harness.addToBattlefieldAndReturn(player1, new BrokenFall());
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(fall);
        harness.activateAbility(player1, idx, null, spider.getId());
        harness.passBothPriorities();

        activate(starke, spider.getId());

        harness.assertOnBattlefield(player2, "Canopy Spider");
        harness.assertNotInGraveyard(player2, "Canopy Spider");
        harness.assertOnBattlefield(player2, "Starke of Rath");
        harness.assertNotOnBattlefield(player1, "Starke of Rath");
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getRegenerationShield()).isZero();
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
