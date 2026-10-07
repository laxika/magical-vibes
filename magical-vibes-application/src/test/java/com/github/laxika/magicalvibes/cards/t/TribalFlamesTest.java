package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.ShivanOasis;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TribalFlames.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class,
        RagingKavu.class, JaceBeleren.class, ShivanOasis.class})
class TribalFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of distinct basic land types controlled")
    void dealsDomainDamageToPlayer() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player2, 20);

        castAtPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals no damage when the controller has no basic land types")
    void noBasicLandTypesDealsNoDamage() {
        harness.setLife(player2, 20);

        castAtPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts only distinct basic land types controlled by the caster")
    void countsDistinctTypesControlledByCasterOnly() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player2, 20);

        castAtPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Each of the five basic land types contributes one damage")
    void countsAllFiveBasicLandTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());
        harness.setLife(player2, 20);

        castAtPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Counts basic land types when the spell resolves")
    void countsDomainAtResolution() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new TribalFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target a creature")
    void canTargetCreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        harness.setHand(player1, List.of(new TribalFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0, kavu.getId());

        harness.assertNotOnBattlefield(player2, "Raging Kavu");
        harness.assertInGraveyard(player2, "Raging Kavu");
    }

    @Test
    @DisplayName("Can target a planeswalker")
    void canTargetPlaneswalker() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);

        harness.setHand(player1, List.of(new TribalFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TribalFlames()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent forest = findPermanent(player2, "Forest");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana-producing lands without basic land types do not contribute to domain")
    void nonbasicLandWithoutBasicTypesDoesNotContribute() {
        harness.addToBattlefield(player1, new ShivanOasis());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player2, 20);

        castAtPlayer2();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Losing a basic land type before resolution reduces damage")
    void domainDecreasesBeforeResolution() {
        harness.addToBattlefield(player1, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new TribalFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(island);
        gd.playerGraveyards.get(player1.getId()).add(island.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private void castAtPlayer2() {
        harness.setHand(player1, List.of(new TribalFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
