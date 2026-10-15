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

        harness.castAdventure(player1, 0, List.of());
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

    @Test
    @DisplayName("Lasting Fayth creates a colorless 1/1 Hero with no lands")
    void createsHeroWithoutCountersWhenControllerHasNoLands() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ZanarkandAncientMetropolis()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent hero = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hero.getCard().isToken()).isTrue();
        assertThat(hero.getCard().getSubtypes()).containsExactly(CardSubtype.HERO);
        assertThat(hero.getCard().getColors()).isEmpty();
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hero.getEffectivePower()).isEqualTo(1);
        assertThat(hero.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Lasting Fayth counts lands at resolution and only counters the new Hero")
    void countsCurrentLandsAndLeavesExistingHeroUnchanged() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ZanarkandAncientMetropolis(), new ZanarkandAncientMetropolis()));
        harness.addMana(player1, ManaColor.GREEN, 12);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent firstHero = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.castAdventure(player1, 0, List.of());
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        Permanent secondHero = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(firstHero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondHero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Zanarkand can be played tapped from exile after its adventure resolves")
    void playsLandFromAdventureExile() {
        ZanarkandAncientMetropolis zanarkand = new ZanarkandAncientMetropolis();
        harness.setHand(player1, List.of(zanarkand));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.castFromExile(player1, zanarkand.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(zanarkand);
        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(zanarkand.getId()))
                .findFirst().orElseThrow();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
