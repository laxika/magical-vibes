package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CoopedUp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegalBunnicorn.class, Forest.class, GrizzlyBears.class, Gingerbrute.class, PropheticPrism.class, CoopedUp.class})
class RegalBunnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Regal Bunnicorn counts itself as a nonland permanent")
    void countsItself() {
        Permanent bunnicorn = addBunnicorn(player1);

        assertStats(bunnicorn, 1, 1);
    }

    @Test
    @DisplayName("Regal Bunnicorn counts your nonland permanents but not lands")
    void countsOwnNonlandPermanents() {
        Permanent bunnicorn = addBunnicorn(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertStats(bunnicorn, 3, 3);
    }

    @Test
    @DisplayName("Regal Bunnicorn ignores an opponent's nonland permanents")
    void ignoresOpponentsNonlandPermanents() {
        Permanent bunnicorn = addBunnicorn(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertStats(bunnicorn, 1, 1);
    }

    @Test
    @DisplayName("Regal Bunnicorn updates as your nonland permanents change")
    void updatesWhenNonlandPermanentsChange() {
        Permanent bunnicorn = addBunnicorn(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertStats(bunnicorn, 2, 2);

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        assertStats(bunnicorn, 1, 1);
    }

    @Test
    @DisplayName("Regal Bunnicorn counts artifacts, attached Auras, and multitype permanents once each")
    void countsAllNonlandPermanentTypes() {
        Permanent bunnicorn = addBunnicorn(player1);
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new Gingerbrute());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CoopedUp());
        aura.setAttachedTo(bunnicorn.getId());
        harness.addToBattlefield(player2, new PropheticPrism());

        assertStats(bunnicorn, 4, 4);
    }

    @Test
    @DisplayName("Regal Bunnicorn adds counters after determining its base stats")
    void countersApplyOnTopOfDynamicStats() {
        Permanent bunnicorn = addBunnicorn(player1);
        bunnicorn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertStats(bunnicorn, 3, 3);

        Permanent prism = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        assertStats(bunnicorn, 4, 4);

        gd.playerBattlefields.get(player1.getId()).remove(prism);
        assertStats(bunnicorn, 3, 3);
    }

    @Test
    @DisplayName("Regal Bunnicorn uses its current controller's nonland permanents")
    void updatesWhenControllerChanges() {
        Permanent bunnicorn = addBunnicorn(player1);
        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new Gingerbrute());
        harness.addToBattlefield(player2, new PropheticPrism());
        assertStats(bunnicorn, 3, 3);

        gd.playerBattlefields.get(player1.getId()).remove(bunnicorn);
        gd.playerBattlefields.get(player2.getId()).add(bunnicorn);

        assertStats(bunnicorn, 2, 2);
    }

    @Test
    @DisplayName("Regal Bunnicorn defines its stats in hand, library, graveyard, and exile without counting itself")
    void definesStatsOutsideBattlefield() {
        RegalBunnicorn inHand = new RegalBunnicorn();
        RegalBunnicorn inLibrary = new RegalBunnicorn();
        RegalBunnicorn inGraveyard = new RegalBunnicorn();
        RegalBunnicorn inExile = new RegalBunnicorn();
        harness.setHand(player1, List.of(inHand));
        harness.setLibrary(player1, List.of(inLibrary));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.setExile(player1, List.of(inExile));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Gingerbrute());

        for (RegalBunnicorn card : List.of(inHand, inLibrary, inGraveyard, inExile)) {
            assertThat(gqs.getEffectiveCardPower(gd, card)).isZero();
            assertThat(gqs.getEffectiveCardToughness(gd, card)).isZero();
        }

        harness.addToBattlefield(player1, new PropheticPrism());
        harness.addToBattlefield(player1, new Gingerbrute());

        for (RegalBunnicorn card : List.of(inHand, inLibrary, inGraveyard, inExile)) {
            assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(2);
            assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(2);
        }
    }

    private Permanent addBunnicorn(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new RegalBunnicorn());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void assertStats(Permanent bunnicorn, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, bunnicorn)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, bunnicorn)).isEqualTo(toughness);
    }
}
