package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoblinGaveleer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HexgoldSledge.class, GoblinGaveleer.class, GrizzlyBears.class})
class HexgoldSledgeTest extends BaseCardTest {

    @Test
    void entersAndConjuresGoblinGaveleerOntoTheBattlefield() {
        harness.setHand(player1, List.of(new HexgoldSledge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent gaveleer = findPermanent(player1, "Goblin Gaveleer");
        assertThat(gaveleer.getCard().isToken()).isFalse();
    }

    @Test
    void equipsAndBoostsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sledge = harness.addToBattlefieldAndReturn(player1, new HexgoldSledge());
        sledge.setSummoningSick(false);
        int sledgeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sledge);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, sledgeIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sledge.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
