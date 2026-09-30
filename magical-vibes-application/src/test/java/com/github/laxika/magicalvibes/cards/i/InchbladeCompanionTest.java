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

    private Permanent addReadyCompanion() {
        Permanent companion = new Permanent(new InchbladeCompanion());
        companion.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(companion);
        return companion;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
