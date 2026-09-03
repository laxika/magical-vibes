package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CreepingMold;
import com.github.laxika.magicalvibes.cards.d.DarajaGriffin;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.cards.s.SisaysRing;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RelicWard.class, SisaysRing.class, CreepingMold.class, DarajaGriffin.class, PhyrexianWalker.class})
class RelicWardTest extends BaseCardTest {

    private Permanent attachWard(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RelicWard());
        aura.setAttachedTo(host.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted artifact has shroud")
    void enchantedArtifactHasShroud() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SisaysRing());
        attachWard(artifact);

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud is lost when Relic Ward leaves the battlefield")
    void shroudLostWhenRemoved() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SisaysRing());
        Permanent aura = attachWard(artifact);

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, artifact, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Enchanted artifact cannot be targeted (shroud)")
    void enchantedArtifactCannotBeTargeted() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SisaysRing());
        attachWard(artifact);

        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Can enchant an artifact creature")
    void canEnchantArtifactCreature() {
        Permanent artifactCreature = addCreatureReady(player1, new PhyrexianWalker());
        harness.setHand(player1, List.of(new RelicWard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, artifactCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Relic Ward itself can still be targeted")
    void auraItselfCanBeTargeted() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SisaysRing());
        Permanent aura = attachWard(artifact);
        harness.setHand(player1, List.of(new CreepingMold()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Relic Ward");
        harness.assertInGraveyard(player1, "Relic Ward");
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Cast at sorcery speed, it stays on the battlefield through cleanup")
    void castAtSorcerySpeedSurvivesCleanup() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SisaysRing());
        harness.setHand(player1, List.of(new RelicWard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertOnBattlefield(player1, "Relic Ward");
    }

    @Test
    @DisplayName("Cast when a sorcery couldn't be cast, its controller sacrifices it at cleanup")
    void castAtInstantSpeedIsSacrificedAtCleanup() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SisaysRing());
        harness.setHand(player1, List.of(new RelicWard()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Relic Ward");

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        harness.assertNotOnBattlefield(player1, "Relic Ward");
        harness.assertInGraveyard(player1, "Relic Ward");
    }

    @Test
    @DisplayName("Cannot enchant a nonartifact permanent")
    void cannotEnchantNonArtifact() {
        Permanent creature = addCreatureReady(player1, new DarajaGriffin());
        harness.setHand(player1, List.of(new RelicWard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }
}
