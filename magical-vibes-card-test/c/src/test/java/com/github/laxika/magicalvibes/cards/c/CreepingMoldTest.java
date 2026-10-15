package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
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
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.PatagiaGolem;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;

@CardUsed({Boomerang.class, CreepingMold.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, PhyrexianArena.class, Millstone.class, PatagiaGolem.class, PhyrexianHulk.class})
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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Millstone()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Resolving destroys target artifact")
    void resolvesDestroyArtifactUpstreamReview() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Millstone()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PatagiaGolem()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Patagia Golem");
        harness.assertInGraveyard(player2, "Patagia Golem");
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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Phyrexian Arena");
        harness.assertInGraveyard(player2, "Phyrexian Arena");
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
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
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
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianHulk());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Phyrexian Hulk");
        harness.assertNotInGraveyard(player2, "Phyrexian Hulk");
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a player with Creeping Mold")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast Creeping Mold without a target")
    void cannotCastWithoutTarget() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Creeping Mold");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A target returned to hand in response is not destroyed")
    void targetReturnedToHandInResponseIsNotDestroyed() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Millstone()).getId();
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Millstone");
        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertNotInGraveyard(player2, "Millstone");
        harness.assertInGraveyard(player1, "Creeping Mold");
        harness.assertInGraveyard(player2, "Boomerang");
        assertThat(gd.stack).isEmpty();
    }
}
