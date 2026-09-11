package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudsteelKirin.class, GrizzlyBears.class})
class CloudsteelKirinTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsFlyingAndGameProtection() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        kirin.setAttachedTo(creature.getId());

        assertThat(gqs.isCreature(gd, kirin)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.canPlayerLoseGame(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerLoseGame(gd, player2.getId())).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheKirin() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(kirin.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, kirin)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(kirin.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, kirin)).isTrue();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent kirin = addCreatureReady(player1, new CloudsteelKirin());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kirin.getAttachedTo()).isNull();
    }
}
