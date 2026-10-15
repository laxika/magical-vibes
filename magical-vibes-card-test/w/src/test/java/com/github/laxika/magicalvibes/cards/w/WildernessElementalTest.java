package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.a.ArcticFlats;
import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildernessElemental.class, AdarkarWastes.class, Forest.class,
        ArcticFlats.class, BorealDruid.class, SnowCoveredForest.class})
class WildernessElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of nonbasic lands opponents control and toughness remains 3")
    void powerCountsOpponentsNonbasicLands() {
        Permanent elemental = addCreatureReady(player1, new WildernessElemental());
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new AdarkarWastes());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power updates as opponents' nonbasic lands enter and leave")
    void powerUpdatesDynamically() {
        Permanent elemental = addCreatureReady(player1, new WildernessElemental());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new AdarkarWastes());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);

        harness.addToBattlefield(player2, new AdarkarWastes());
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);

        gd.playerBattlefields.get(player2.getId()).remove(firstLand);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power is zero when opponents control no nonbasic lands")
    void powerIsZeroWithOnlyBasicLands() {
        Permanent elemental = addCreatureReady(player1, new WildernessElemental());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, elemental)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Snow nonbasic lands count, but snow basic lands and nonland permanents do not")
    void distinguishesSnowFromBasic() {
        Permanent elemental = addCreatureReady(player1, new WildernessElemental());
        harness.addToBattlefield(player2, new ArcticFlats());
        harness.addToBattlefield(player2, new SnowCoveredForest());
        harness.addToBattlefield(player2, new BorealDruid());
        harness.addToBattlefield(player1, new ArcticFlats());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("The power-defining ability works in hand and graveyard")
    void powerIsDefinedOutsideBattlefield() {
        WildernessElemental inHand = new WildernessElemental();
        WildernessElemental inGraveyard = new WildernessElemental();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.addToBattlefield(player2, new ArcticFlats());
        harness.addToBattlefield(player2, new ArcticFlats());
        harness.addToBattlefield(player1, new ArcticFlats());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(2);

        harness.addToBattlefield(player2, new ArcticFlats());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Trample deals excess damage using the power defined by opposing nonbasic lands")
    void tramplesWithDynamicPower() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WildernessElemental());
        Permanent blocker = addCreatureReady(player2, new BorealDruid());
        harness.addToBattlefield(player2, new ArcticFlats());
        harness.addToBattlefield(player2, new ArcticFlats());
        harness.addToBattlefield(player2, new ArcticFlats());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Boreal Druid");
        harness.assertOnBattlefield(player1, "Wilderness Elemental");
    }

}
