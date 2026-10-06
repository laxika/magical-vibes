package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShortBow.class, GrizzlyBears.class})
class ShortBowTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {1} attaches Short Bow to target creature you control")
    void equipAttachesToCreature() {
        Permanent bow = addBowReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 and has reach and vigilance")
    void equippedCreatureGetsBoostAndKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bow = addBowReady(player1);
        bow.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Unattached Short Bow does not affect creatures")
    void unattachedBowGrantsNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addBowReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses Short Bow's effects when it becomes unattached")
    void effectsStopWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bow = addBowReady(player1);
        bow.setAttachedTo(creature.getId());

        bow.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    private Permanent addBowReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ShortBow());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void reEquippingMovesAllBenefitsToTheNewCreature() {
        Permanent bow = addBowReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        bow.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void equippedCreatureKeepsBenefitsAfterChangingController() {
        Permanent bow = addBowReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        bow.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.runStateBasedActions();

        assertThat(bow.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void failedReEquipLeavesOriginalAttachmentIntact() {
        Permanent bow = addBowReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        bow.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, second.getId());

        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent bow = addBowReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bow.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipWithoutMana() {
        Permanent bow = addBowReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(bow.getAttachedTo()).isNull();
    }

    @Test
    void equipFailsIfTargetChangesControllerBeforeResolution() {
        Permanent bow = addBowReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void equippedCreatureDoesNotTapWhenAttacking() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent bow = addBowReady(player1);
        bow.setAttachedTo(creature.getId());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    void cannotEquipDuringOpponentsTurn() {
        addBowReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        addBowReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void cannotEquipWhileAnotherEquipAbilityIsOnTheStack() {
        addBowReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();
    }
}
