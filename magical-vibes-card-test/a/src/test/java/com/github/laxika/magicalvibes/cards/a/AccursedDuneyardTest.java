package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AccursedDuneyard.class, DrudgeSkeletons.class, GrizzlyBears.class})
class AccursedDuneyardTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        Permanent duneyard = addDuneyard();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(duneyard.isTapped()).isTrue();
    }

    @Test
    void regeneratesAllowedCreatureSubtype() {
        addDuneyard();
        Permanent skeletons = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, skeletons.getId());
        harness.passBothPriorities();

        assertThat(skeletons.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void rejectsCreatureWithAnUnlistedSubtype() {
        addDuneyard();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addDuneyard() {
        Permanent duneyard = new Permanent(new AccursedDuneyard());
        duneyard.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(duneyard);
        return duneyard;
    }
}
