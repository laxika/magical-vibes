package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CollectorOuphe.class, IronMyr.class, MindStone.class, ProdigalSorcerer.class})
class CollectorOupheTest extends BaseCardTest {

    @Test
    void blocksArtifactManaAbilities() {
        harness.addToBattlefield(player1, new CollectorOuphe());
        Permanent ironMyr = harness.addToBattlefieldAndReturn(player2, new IronMyr());
        ironMyr.setSummoningSick(false);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Collector Ouphe");
    }

    @Test
    void blocksNonManaArtifactAbilities() {
        harness.addToBattlefield(player1, new CollectorOuphe());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());
        mindStone.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Collector Ouphe");
    }

    @Test
    void doesNotBlockNonArtifactAbilities() {
        harness.addToBattlefield(player1, new CollectorOuphe());
        Permanent prodigalSorcerer = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        prodigalSorcerer.setSummoningSick(false);

        harness.activateAbility(player2, 0, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }
}
