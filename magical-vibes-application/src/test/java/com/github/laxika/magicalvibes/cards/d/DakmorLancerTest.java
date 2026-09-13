package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.m.Meekstone;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DakmorLancer.class, GrizzlyBears.class, Meekstone.class, PhyrexianHulk.class, ScatheZombies.class})
class DakmorLancerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB resolves and destroys target nonblack creature")
    void etbDestroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DakmorLancer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Dakmor Lancer");
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);

        // Resolve ETB
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can destroy an artifact creature (only nonblack restricted)")
    void canDestroyArtifactCreature() {
        harness.addToBattlefield(player2, new PhyrexianHulk());
        harness.setHand(player1, List.of(new DakmorLancer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Phyrexian Hulk");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Phyrexian Hulk");
    }

    @Test
    @DisplayName("Destroyed creature can be regenerated")
    void destroyedCreatureCanBeRegenerated() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DakmorLancer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        target.setRegenerationShield(1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new DakmorLancer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Scathe Zombies");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Meekstone());
        harness.setHand(player1, List.of(new DakmorLancer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Meekstone");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack creature");
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DakmorLancer()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
