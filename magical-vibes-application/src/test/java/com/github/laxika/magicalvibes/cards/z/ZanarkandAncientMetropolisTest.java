package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LastingFayth;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZanarkandAncientMetropolis.class, LastingFayth.class, Forest.class})
class ZanarkandAncientMetropolisTest extends BaseCardTest {

    @Test
    @DisplayName("Zanarkand enters tapped and produces green mana")
    void entersTappedAndProducesGreenMana() {
        harness.setHand(player1, List.of(new ZanarkandAncientMetropolis()));

        harness.playLand(player1, 0);
        Permanent zanarkand = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(zanarkand.isTapped()).isTrue();

        zanarkand.untap();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lasting Fayth creates a Hero with one counter per land controlled")
    void lastingFaythCreatesHeroWithCountersForControlledLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        ZanarkandAncientMetropolis zanarkand = new ZanarkandAncientMetropolis();
        harness.setHand(player1, List.of(zanarkand));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCardWithAdventure(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent hero = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.HERO))
                .findFirst()
                .orElseThrow();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hero.getEffectivePower()).isEqualTo(4);
        assertThat(hero.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(zanarkand);
    }
}
