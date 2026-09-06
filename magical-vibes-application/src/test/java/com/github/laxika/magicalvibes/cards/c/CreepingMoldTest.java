package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GossamerChains;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.cards.s.SisaysRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CreepingMold.class, SisaysRing.class, GossamerChains.class, Quicksand.class, Python.class,
        PhyrexianWalker.class})
class CreepingMoldTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Creeping Mold puts it on stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new Quicksand());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Quicksand");
        harness.castSorcery(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard()).isInstanceOf(CreepingMold.class);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target artifact")
    void resolvesDestroyArtifact() {
        harness.addToBattlefield(player2, new SisaysRing());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Sisay's Ring");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sisay's Ring");
        harness.assertInGraveyard(player2, "Sisay's Ring");
    }

    @Test
    @DisplayName("Resolving destroys target artifact creature")
    void resolvesDestroyArtifactCreature() {
        harness.addToBattlefield(player2, new PhyrexianWalker());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Phyrexian Walker");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Phyrexian Walker");
        harness.assertInGraveyard(player2, "Phyrexian Walker");
    }

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesDestroyEnchantment() {
        harness.addToBattlefield(player2, new GossamerChains());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Gossamer Chains");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gossamer Chains");
        harness.assertInGraveyard(player2, "Gossamer Chains");
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvesDestroyLand() {
        harness.addToBattlefield(player2, new Quicksand());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Quicksand");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Quicksand");
        harness.assertInGraveyard(player2, "Quicksand");
    }

    @Test
    @DisplayName("Can destroy own permanent")
    void canDestroyOwnPermanent() {
        harness.addToBattlefield(player1, new Quicksand());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Quicksand");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Quicksand");
        harness.assertInGraveyard(player1, "Quicksand");
    }

    @Test
    @DisplayName("Fizzles if target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new Quicksand());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Quicksand");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Creeping Mold");
    }

    @Test
    @DisplayName("Cannot destroy creature with Creeping Mold")
    void cannotDestroyCreature() {
        harness.addToBattlefield(player2, new Python());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID creatureId = harness.getPermanentId(player2, "Python");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
