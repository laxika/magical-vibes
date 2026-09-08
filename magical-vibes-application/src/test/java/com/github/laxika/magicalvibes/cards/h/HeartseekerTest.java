package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent heartseeker = addHeartseekerReady(player1);
        heartseeker.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature can tap and unattach Heartseeker to destroy a target creature")
    void destroysTargetCreatureAndUnattaches() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent heartseeker = addHeartseekerReady(player1);
        heartseeker.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(heartseeker.getAttachedTo()).isNull();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Heartseeker's ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent heartseeker = addHeartseekerReady(player1);
        heartseeker.setAttachedTo(creature.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(heartseeker.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature loses Heartseeker's ability when Heartseeker is unattached")
    void losesAbilityWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent heartseeker = addHeartseekerReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(creature.isTapped()).isFalse();
        assertThat(heartseeker.getAttachedTo()).isNull();
    }

    private Permanent addHeartseekerReady(Player player) {
        Permanent permanent = new Permanent(new Heartseeker());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
