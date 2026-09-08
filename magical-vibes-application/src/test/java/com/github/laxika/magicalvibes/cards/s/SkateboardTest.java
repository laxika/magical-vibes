package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skateboard.class, Forest.class, GrizzlyBears.class})
class SkateboardTest extends BaseCardTest {

    @Test
    void entersAndTapsTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Skateboard()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Skateboard");
    }

    @Test
    void equippedCreatureGetsPlusOnePowerAndHaste() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent skateboard = harness.addToBattlefieldAndReturn(player1, new Skateboard());
        skateboard.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void equipAttachesSkateboardToCreatureYouControl() {
        Permanent skateboard = harness.addToBattlefieldAndReturn(player1, new Skateboard());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(skateboard.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void losesBonusWhenSkateboardIsRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent skateboard = harness.addToBattlefieldAndReturn(player1, new Skateboard());
        skateboard.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(skateboard);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new Skateboard()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }
}
