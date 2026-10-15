package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleetfeatherSandals.class, BronzeSable.class})
class FleetfeatherSandalsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has flying and haste")
    void equippedCreatureHasFlyingAndHaste() {
        Permanent sandals = harness.addToBattlefieldAndReturn(player1, new FleetfeatherSandals());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BronzeSable());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();

        sandals.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Keywords are lost when the Sandals are unattached")
    void keywordsLostWhenUnattached() {
        Permanent sandals = harness.addToBattlefieldAndReturn(player1, new FleetfeatherSandals());

        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        sandals.setAttachedTo(bears.getId());

        sandals.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Only the equipped creature gains flying and haste")
    void onlyEquippedCreatureGainsKeywords() {
        Permanent sandals = harness.addToBattlefieldAndReturn(player1, new FleetfeatherSandals());

        Permanent equipped = harness.addToBattlefieldAndReturn(player1, new BronzeSable());

        Permanent other = harness.addToBattlefieldAndReturn(player1, new BronzeSable());

        sandals.setAttachedTo(equipped.getId());

        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }

    @Test
    void equipResolvesAndCanMoveToAnotherCreature() {
        Permanent sandals = harness.addToBattlefieldAndReturn(player1, new FleetfeatherSandals());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        assertThat(sandals.isAttached()).isFalse();
        harness.passBothPriorities();

        assertThat(sandals.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(sandals.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(sandals.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
    }

    @Test
    void equipRequiresTwoMana() {
        harness.addToBattlefield(player1, new FleetfeatherSandals());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new FleetfeatherSandals());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetNoncreature() {
        Permanent sandals = harness.addToBattlefieldAndReturn(player1, new FleetfeatherSandals());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sandals.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sandals.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedDuringUpkeep() {
        harness.addToBattlefield(player1, new FleetfeatherSandals());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void failedReequipLeavesOriginalCreatureEquipped() {
        Permanent sandals = harness.addToBattlefieldAndReturn(player1, new FleetfeatherSandals());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        sandals.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(sandals.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.hasKeyword(gd, original, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, original, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equippedNewCreatureCanAttackAndLosesHasteWhenEquipmentLeaves() {
        Permanent sandals = harness.addToBattlefieldAndReturn(player1, new FleetfeatherSandals());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        creature.setSummoningSick(true);
        assertThat(als.canAttack(gd, creature, player1.getId())).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, creature, player1.getId())).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(sandals);

        assertThat(als.canAttack(gd, creature, player1.getId())).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void equipCannotBeActivatedWhileStackIsNotEmpty() {
        harness.addToBattlefield(player1, new FleetfeatherSandals());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BronzeSable());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }
}
