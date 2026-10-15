package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FireDiamond;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NevinyrralsDisk;
import com.github.laxika.magicalvibes.cards.s.SteelExemplar;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalPilgrim;
import com.github.laxika.magicalvibes.cards.t.ThranSpider;
import com.github.laxika.magicalvibes.cards.u.UrzaPowerstoneProdigy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrotherhoodsEnd.class, ChandraNalaar.class, FireDiamond.class, GrizzlyBears.class, NevinyrralsDisk.class, SteelExemplar.class, TeferiTemporalPilgrim.class, ThranSpider.class, UrzaPowerstoneProdigy.class})
class BrotherhoodsEndTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 3 damage to each creature and planeswalker")
    void damageModeDamagesCreaturesAndPlaneswalkers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new BrotherhoodsEnd()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
    }

    @Test
    @DisplayName("Artifact mode destroys artifacts with mana value 3 or less only")
    void artifactModeDestroysArtifactsWithinManaValue() {
        harness.addToBattlefield(player1, new FireDiamond());
        harness.addToBattlefield(player2, new NevinyrralsDisk());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrotherhoodsEnd()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fire Diamond");
        harness.assertOnBattlefield(player2, "Nevinyrral's Disk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage mode does not damage players")
    void damageModeDoesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BrotherhoodsEnd()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage mode marks exactly three damage and removes three loyalty from survivors")
    void damageModeLeavesSurvivorsWithThreeDamageOrThreeFewerLoyalty() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteelExemplar());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new TeferiTemporalPilgrim());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player2, new UrzaPowerstoneProdigy());
        harness.setHand(player1, List.of(new BrotherhoodsEnd()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Steel Exemplar");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Teferi, Temporal Pilgrim");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Urza, Powerstone Prodigy");
    }

    @Test
    @DisplayName("Artifact mode destroys mana value three artifact creatures on both sides without damage")
    void artifactModeIncludesThreeManaArtifactsAndSparedPermanentsTakeNoDamage() {
        harness.addToBattlefield(player1, new ThranSpider());
        harness.addToBattlefield(player2, new ThranSpider());
        Permanent largeArtifact = harness.addToBattlefieldAndReturn(player2, new SteelExemplar());
        Permanent nonartifact = harness.addToBattlefieldAndReturn(player2, new UrzaPowerstoneProdigy());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new TeferiTemporalPilgrim());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new BrotherhoodsEnd()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Thran Spider");
        harness.assertNotOnBattlefield(player2, "Thran Spider");
        harness.assertInGraveyard(player1, "Thran Spider");
        harness.assertInGraveyard(player2, "Thran Spider");
        harness.assertOnBattlefield(player2, "Steel Exemplar");
        assertThat(largeArtifact.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Urza, Powerstone Prodigy");
        assertThat(nonartifact.getMarkedDamage()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }
}
