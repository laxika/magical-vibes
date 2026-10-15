package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnctussRetrofitter.class, PropheticPrism.class})
class UnctussRetrofitterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes an artifact you control a 4/4 artifact creature")
    void animatesTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        UUID artifactId = artifact.getId();
        castRetrofitter(List.of(artifactId));

        GameData gd = harness.getGameData();
        Permanent target = gqs.findPermanentById(gd, artifactId);

        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("The animation ends when Unctus's Retrofitter leaves the battlefield")
    void animationEndsWhenSourceLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        UUID artifactId = artifact.getId();
        castRetrofitter(List.of(artifactId));

        GameData gd = harness.getGameData();
        Permanent source = findPermanent(player1, "Unctus's Retrofitter");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        Permanent target = gqs.findPermanentById(gd, artifactId);
        assertThat(gqs.isCreature(gd, target)).isFalse();
    }

    @Test
    @DisplayName("The ETB ability can choose no artifact")
    void canChooseNoArtifact() {
        harness.setHand(player1, List.of(new UnctussRetrofitter()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Unctus's Retrofitter");
    }

    @Test
    @DisplayName("The ETB ability cannot target an artifact an opponent controls")
    void cannotTargetOpponentsArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PropheticPrism()).getId();
        harness.setHand(player1, List.of(new UnctussRetrofitter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact you control");
    }

    @Test
    @DisplayName("Toxic 1 adds one poison counter alongside combat damage without using the stack")
    void combatDamageGivesPoison() {
        Permanent attacker = addCreatureReady(player1, new UnctussRetrofitter());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Toxic gives one poison counter even when counters increase combat damage")
    void increasedCombatDamageStillGivesOnePoison() {
        Permanent attacker = addCreatureReady(player1, new UnctussRetrofitter());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("The animation does not start if its source leaves before the trigger resolves")
    void sourceLeavesBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new UnctussRetrofitter()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Unctus's Retrofitter");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counters modify the animated artifact's base power and toughness")
    void animationKeepsPowerToughnessCounters() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castRetrofitter(List.of(artifact.getId()));

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(artifact.getEffectivePower()).isEqualTo(6);
        assertThat(artifact.getEffectiveToughness()).isEqualTo(6);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A target that leaves before resolution is not animated")
    void targetLeavesBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of(new UnctussRetrofitter()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Prophetic Prism");
        harness.assertOnBattlefield(player1, "Unctus's Retrofitter");
    }

    private void castRetrofitter(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new UnctussRetrofitter()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, targetIds);
        resolveAllTriggers();
    }
}
