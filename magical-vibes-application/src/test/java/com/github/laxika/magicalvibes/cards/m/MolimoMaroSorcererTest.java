package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MolimoMaroSorcerer.class, Forest.class, Plains.class, GloriousAnthem.class, GiantSpider.class})
class MolimoMaroSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Molimo puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new MolimoMaroSorcerer(), "{4}{G}{G}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Molimo dies to state-based actions with no lands")
    void diesWithNoLands() {
        harness.castFromHand(player1, new MolimoMaroSorcerer(), "{4}{G}{G}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Molimo, Maro-Sorcerer");
        harness.assertInGraveyard(player1, "Molimo, Maro-Sorcerer");
    }

    @Test
    @DisplayName("Molimo survives when you control a land")
    void survivesWithLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new MolimoMaroSorcerer(), "{4}{G}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Molimo, Maro-Sorcerer");
    }

    @Test
    @DisplayName("Molimo power and toughness equal lands you control")
    void ptEqualsControlledLands() {
        Permanent molimo = addCreatureReady(player1, new MolimoMaroSorcerer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(3);
    }

    @Test
    @DisplayName("Molimo counts only your lands, not opponent lands")
    void countsOnlyControllersLands() {
        Permanent molimo = addCreatureReady(player1, new MolimoMaroSorcerer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Molimo power and toughness update when lands change")
    void ptUpdatesWhenLandsChange() {
        Permanent molimo = addCreatureReady(player1, new MolimoMaroSorcerer());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(1);

        harness.addToBattlefield(player1, new Plains());
        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(0);
    }

    @Test
    @DisplayName("Molimo characteristic-defining P/T stacks with other static bonuses")
    void ptStacksWithOtherStaticBonuses() {
        Permanent molimo = addCreatureReady(player1, new MolimoMaroSorcerer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(3);
    }

    @Test
    @DisplayName("Molimo tramples over a blocker using its current land-based power")
    void tramplesOverBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MolimoMaroSorcerer());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Molimo, Maro-Sorcerer");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Molimo defines its power and toughness in hand and graveyard using its owner's lands")
    void definesPowerAndToughnessOutsideBattlefield() {
        MolimoMaroSorcerer molimo = new MolimoMaroSorcerer();
        harness.setHand(player1, List.of(molimo));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectiveCardPower(gd, molimo)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, molimo)).isEqualTo(1);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(molimo));
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectiveCardPower(gd, molimo)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, molimo)).isEqualTo(2);
    }

    @Test
    @DisplayName("Glorious Anthem keeps Molimo alive even with no lands")
    void survivesWithoutLandsWithStaticBonus() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.castFromHand(player1, new MolimoMaroSorcerer(), "{4}{G}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Molimo, Maro-Sorcerer");
        Permanent molimo = findPermanent(player1, "Molimo, Maro-Sorcerer");
        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(1);
    }

}
