package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GolemSkinGauntlets.class, LeoninScimitar.class, AlphaMyr.class})
class GolemSkinGauntletsTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusOnePowerPerAttachedEquipment() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent gauntlets = harness.addToBattlefieldAndReturn(player1, new GolemSkinGauntlets());
        gauntlets.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        scimitar.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void unattachedGauntletsDoNotBoostACreature() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addToBattlefieldAndReturn(player1, new GolemSkinGauntlets());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void equipAttachesGauntletsToCreatureYouControl() {
        Permanent gauntlets = harness.addToBattlefieldAndReturn(player1, new GolemSkinGauntlets());
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gauntlets.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent gauntlets = harness.addToBattlefieldAndReturn(player1, new GolemSkinGauntlets());
        Permanent opponentCreature = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gauntlets.getAttachedTo()).isNull();
    }
}
