package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Rope.class, GrizzlyBears.class, Forest.class, EnsoulArtifact.class})
class RopeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+2, reach, and a one-blocker cap")
    void equippedCreatureGetsAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent rope = addRopeReady(player1);
        rope.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rope's static abilities disappear when it is unattached")
    void effectsDisappearWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent rope = addRopeReady(player1);
        rope.setAttachedTo(creature.getId());

        rope.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Equip {3} attaches Rope to a creature")
    void equipAttachesRope() {
        Permanent rope = addRopeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(rope.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Paying {2} and sacrificing Rope draws a card")
    void sacrificeAbilityDrawsCard() {
        Permanent rope = addRopeReady(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rope);
        harness.assertInGraveyard(player1, "Rope");
        harness.assertInHand(player1, "Forest");
    }

    private Permanent addRopeReady(Player player) {
        return addCreatureReady(player, new Rope());
    }

    @Test
    @DisplayName("Re-equipping moves all benefits to the new creature")
    void reEquipMovesBenefits() {
        Permanent rope = addRopeReady(player1);
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        Permanent replacement = addCreatureReady(player1, new GrizzlyBears());
        rope.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, replacement.getId());
        harness.passBothPriorities();

        assertThat(rope.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, original, Keyword.REACH)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, original)).isEqualTo(Integer.MAX_VALUE);
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.REACH)).isTrue();
        assertThat(gqs.getMaxBlockersAllowed(gd, replacement)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing attached Rope removes benefits before the draw resolves")
    void sacrificeRemovesBenefitsAsCost() {
        Permanent rope = addRopeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        rope.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Rope");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(Integer.MAX_VALUE);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("An equipped attacker cannot be blocked by two creatures")
    void equippedAttackerRejectsTwoBlockers() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent rope = addRopeReady(player1);
        rope.setAttachedTo(creature.getId());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Rope animated by Ensoul Artifact may be blocked by two creatures")
    void animatedRopeDoesNotRestrictItsOwnBlockers() {
        Permanent rope = addRopeReady(player1);
        harness.setHand(player1, List.of(new EnsoulArtifact()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, rope.getId());
        harness.passBothPriorities();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(Permanent::isBlocking);
    }
}
