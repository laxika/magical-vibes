package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WitchesEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Witches' Eye to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent eye = new Permanent(new WitchesEye());
        gd.playerBattlefields.get(player1.getId()).add(eye);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(eye.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can pay one and tap to scry 1")
    void equippedCreatureCanScry() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent eye = new Permanent(new WitchesEye());
        eye.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(eye);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("An unattached Witches' Eye grants no ability")
    void unattachedEyeGrantsNoAbility() {
        addCreatureReady(player1, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new WitchesEye()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
