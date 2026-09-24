package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmyRose.class, GrizzlyBears.class, LeoninScimitar.class})
class AmyRoseTest extends BaseCardTest {

    @Test
    void attachesEquipmentAndBoostsAnotherAttackerByAmysPower() {
        Permanent amy = addCreatureReady(player1, new AmyRose());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(equipment.getId())
                .doesNotContain(amy.getId(), otherAttacker.getId());
        harness.handlePermanentChosen(player1, equipment.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(otherAttacker.getId())
                .doesNotContain(amy.getId());
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(amy.getId());
        assertThat(gqs.getEffectivePower(gd, amy)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(6);
    }
}
