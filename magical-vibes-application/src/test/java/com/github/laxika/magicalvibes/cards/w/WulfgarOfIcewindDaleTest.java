package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JovensFerrets;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WulfgarOfIcewindDale.class, JovensFerrets.class, GrizzlyBears.class})
class WulfgarOfIcewindDaleTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles attack triggers of permanents you control")
    void doublesAttackTriggers() {
        addCreatureReady(player1, new WulfgarOfIcewindDale());
        Permanent ferrets = addCreatureReady(player1, new JovensFerrets());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(ferrets.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Melee boosts Wulfgar when it attacks")
    void meleeBoostsAttackingWulfgar() {
        Permanent wulfgar = addCreatureReady(player1, new WulfgarOfIcewindDale());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(wulfgar.getPowerModifier()).isEqualTo(1);
        assertThat(wulfgar.getToughnessModifier()).isEqualTo(1);
    }
}
