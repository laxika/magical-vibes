package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FledglingOspreyTest extends BaseCardTest {

    @Test
    @DisplayName("Fledgling Osprey does not have flying while unenchanted")
    void doesNotHaveFlyingWhileUnenchanted() {
        Permanent osprey = new Permanent(new FledglingOsprey());
        gd.playerBattlefields.get(player1.getId()).add(osprey);

        assertThat(gqs.hasKeyword(gd, osprey, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Fledgling Osprey has flying while enchanted")
    void hasFlyingWhileEnchanted() {
        Permanent osprey = new Permanent(new FledglingOsprey());
        Permanent aura = new Permanent(new HolyStrength());
        aura.setAttachedTo(osprey.getId());
        gd.playerBattlefields.get(player1.getId()).add(osprey);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, osprey, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Fledgling Osprey loses flying when the Aura leaves")
    void losesFlyingWhenAuraLeaves() {
        Permanent osprey = new Permanent(new FledglingOsprey());
        Permanent aura = new Permanent(new HolyStrength());
        aura.setAttachedTo(osprey.getId());
        gd.playerBattlefields.get(player1.getId()).add(osprey);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, osprey, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, osprey, Keyword.FLYING)).isFalse();
    }
}
