package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CrimsonAcolyte;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrimsonAcolyte.class, FallingStar.class, Island.class, WallOfStone.class})
class FallingStarTest extends BaseCardTest {

    @Test
    void damagesAndTapsCreaturesButNotLands() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WallOfStone());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new CrimsonAcolyte());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new FallingStar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(creature.isTapped()).isTrue();
        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(protectedCreature.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
    }
}
