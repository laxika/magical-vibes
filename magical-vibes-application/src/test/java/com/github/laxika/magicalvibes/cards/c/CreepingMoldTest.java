package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GossamerChains;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.cards.s.SisaysRing;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CreepingMold.class, Forest.class, GloriousAnthem.class, GossamerChains.class, GrizzlyBears.class, PhyrexianHulk.class, PhyrexianWalker.class, Python.class, Quicksand.class, SisaysRing.class})
class CreepingMoldTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Creeping Mold puts it on stack with target")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SisaysRing()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Sisay's Ring");
        harness.assertInGraveyard(player2, "Sisay's Ring");
    }

    @Test
    @DisplayName("Resolving destroys target artifact")
    void resolvesDestroyArtifactUpstreamReview() {
        harness.addToBattlefield(player2, new SisaysRing());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Sisay's Ring");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Sisay's Ring");
        harness.assertInGraveyard(player2, "Sisay's Ring");
    }

    @Test
    @DisplayName("Resolving destroys target artifact creature")
    void resolvesDestroyArtifactCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PhyrexianHulk()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Phyrexian Hulk");
        harness.assertInGraveyard(player2, "Phyrexian Hulk");
    }

    @Test
    @DisplayName("Resolving destroys target artifact creature")
    void resolvesDestroyArtifactCreatureUpstreamReview() {
        harness.addToBattlefield(player2, new PhyrexianWalker());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Phyrexian Walker");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Phyrexian Walker");
        harness.assertInGraveyard(player2, "Phyrexian Walker");
    }

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesDestroyEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Resolving destroys target enchantment")
    void resolvesDestroyEnchantmentUpstreamReview() {
        harness.addToBattlefield(player2, new GossamerChains());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Gossamer Chains");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Gossamer Chains");
        harness.assertInGraveyard(player2, "Gossamer Chains");
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvesDestroyLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvesDestroyLandUpstreamReview() {
        harness.addToBattlefield(player2, new Quicksand());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Quicksand");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Quicksand");
        harness.assertInGraveyard(player2, "Quicksand");
    }

    @Test
    @DisplayName("Can destroy own permanent")
    void canDestroyOwnPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Can destroy own permanent")
    void canDestroyOwnPermanentUpstreamReview() {
        harness.addToBattlefield(player1, new Quicksand());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player1, "Quicksand");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Quicksand");
        harness.assertInGraveyard(player1, "Quicksand");
    }

    @Test
    @DisplayName("Fizzles if target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Creeping Mold");
    }

    @Test
    @DisplayName("Cannot destroy creature with Creeping Mold")
    void cannotDestroyCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regeneration prevents Creeping Mold from destroying the target")
    void regenerationPreventsDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianWalker());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Phyrexian Walker");
        harness.assertNotInGraveyard(player2, "Phyrexian Walker");
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.isTapped()).isTrue();
    }
}
