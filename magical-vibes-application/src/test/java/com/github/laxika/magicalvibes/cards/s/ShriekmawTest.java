package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shriekmaw.class, GrizzlyBears.class, Ornithopter.class, RavenousRats.class})
class ShriekmawTest extends BaseCardTest {

    // ===== Hardcast =====

    @Test
    @DisplayName("Hardcast: ETB destroys target nonblack, nonartifact creature and Shriekmaw stays")
    void hardcastDestroysCreatureAndStays() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Shriekmaw");
    }

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: paying {1}{B}, ETB destroys the target and Shriekmaw is sacrificed")
    void evokeDestroysAndSacrificesSelf() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreatureWithEvoke(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Shriekmaw");
        harness.assertInGraveyard(player1, "Shriekmaw");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Illegal targets =====

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new RavenousRats());
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID blackId = harness.getPermanentId(player2, "Ravenous Rats");
        assertThatThrownBy(() ->
                harness.castCreature(player1, 0, blackId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID artifactId = harness.getPermanentId(player2, "Ornithopter");
        assertThatThrownBy(() ->
                harness.castCreature(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Evoke sacrifice can resolve before destruction, which still resolves afterward")
    void controllerCanChooseSacrificeBeforeDestruction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2: Shriekmaw's ETB ability");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shriekmaw");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shriekmaw can be evoked with no legal destruction target")
    void evokeWithoutLegalTargetStillSacrificesSelf() {
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Shriekmaw");
        harness.assertNotOnBattlefield(player1, "Shriekmaw");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hardcasting without a legal destruction target leaves Shriekmaw on the battlefield")
    void hardcastWithoutLegalTargetStays() {
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Shriekmaw");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mandatory destruction ability can target its controller's creature")
    void destroysOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shriekmaw()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Shriekmaw");
    }

    @Test
    @DisplayName("Fear permits black and artifact blockers but excludes other blockers")
    void fearRestrictsBlockers() {
        var attacker = addCreatureReady(player1, new Shriekmaw());
        var greenBlocker = addCreatureReady(player2, new GrizzlyBears());
        var blackBlocker = addCreatureReady(player2, new RavenousRats());
        var artifactBlocker = addCreatureReady(player2, new Ornithopter());
        var defenders = gd.playerBattlefields.get(player2.getId());

        assertThat(bls.canBlockAttacker(gd, greenBlocker, attacker, defenders)).isFalse();
        assertThat(bls.canBlockAttacker(gd, blackBlocker, attacker, defenders)).isTrue();
        assertThat(bls.canBlockAttacker(gd, artifactBlocker, attacker, defenders)).isTrue();
    }
}
