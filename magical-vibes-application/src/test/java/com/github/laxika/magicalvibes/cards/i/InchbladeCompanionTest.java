package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InchbladeCompanion.class, GrizzlyBears.class})
class InchbladeCompanionTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusOnePlusOneAndCompanionStopsBeingACreature() {
        Permanent companion = addReadyCompanion();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        companion.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, companion)).isFalse();
    }

    @Test
    void attachmentCreatesOneCopyAndTheCopyHasNoAttachmentTrigger() {
        Permanent companion = addReadyCompanion();
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, battlefieldIndex(companion), 0, null, firstCreature.getId());
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.activateAbility(player1, battlefieldIndex(token), 0, null, secondCreature.getId());
        resolveAllTriggers();

        harness.activateAbility(player1, battlefieldIndex(companion), 1, null, null);
        resolveAllTriggers();
        harness.activateAbility(player1, battlefieldIndex(companion), 0, null, firstCreature.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent companion = addReadyCompanion();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(companion), 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(companion.getAttachedTo()).isNull();
    }

    @Test
    void reconfigureCannotTargetItself() {
        Permanent companion = addReadyCompanion();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(companion), 0, null, companion.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(companion.getAttachedTo()).isNull();
    }

    @Test
    void unattachRestoresCreatureAndRemovesTheBoostWithoutCreatingAnotherToken() {
        Permanent companion = addReadyCompanion();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(companion), 0, null, creature.getId());
        resolveAllTriggers();
        assertThat(companion.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, companion)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);

        harness.activateAbility(player1, battlefieldIndex(companion), 1, null, null);
        resolveAllTriggers();

        assertThat(companion.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, companion)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    void reconfiguringToTheSameCreatureDoesNotCreateAnotherToken() {
        Permanent companion = addReadyCompanion();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(companion), 0, null, creature.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, battlefieldIndex(companion), 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(companion.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    void reconfigureRequiresTwoMana() {
        Permanent companion = addReadyCompanion();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(companion), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(companion.getAttachedTo()).isNull();
    }

    private Permanent addReadyCompanion() {
        return addCreatureReady(player1, new InchbladeCompanion());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
