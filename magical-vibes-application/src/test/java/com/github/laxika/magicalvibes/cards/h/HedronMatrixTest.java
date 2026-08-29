package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HedronMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +X/+X equal to its mana value")
    void equippedCreatureGetsItsManaValueAsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent matrix = addMatrixReady(player1);
        matrix.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost uses the equipped creature's current mana value")
    void boostUsesEquippedCreatureManaValue() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        Permanent matrix = addMatrixReady(player1);
        matrix.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unattached Hedron Matrix does not boost creatures")
    void unattachedMatrixDoesNotBoostCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addMatrixReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private Permanent addMatrixReady(Player player) {
        Permanent permanent = new Permanent(new HedronMatrix());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
