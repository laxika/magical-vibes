package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Rope.class, GrizzlyBears.class, Forest.class})
class RopeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+2, reach, and a one-blocker cap")
    void equippedCreatureGetsAbilities() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent rope = addRopeReady(player1);
        rope.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rope's static abilities disappear when it is unattached")
    void effectsDisappearWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent rope = addRopeReady(player1);
        rope.setAttachedTo(creature.getId());

        rope.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.getMaxBlockersAllowed(gd, creature)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("Equip {3} attaches Rope to a creature")
    void equipAttachesRope() {
        Permanent rope = addRopeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(rope.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Paying {2} and sacrificing Rope draws a card")
    void sacrificeAbilityDrawsCard() {
        Permanent rope = addRopeReady(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rope);
        harness.assertInGraveyard(player1, "Rope");
        harness.assertInHand(player1, "Forest");
    }

    private Permanent addRopeReady(Player player) {
        Permanent permanent = new Permanent(new Rope());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
