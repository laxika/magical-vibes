package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed(WildColos.class)
class WildColosTest extends BaseCardTest {

    @Test
    @DisplayName("Wild Colos can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        Permanent colos = new Permanent(new WildColos());
        colos.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(colos);

        declareAttackers(List.of(0));
    }
}
