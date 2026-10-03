package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdmiralBeckettBrass;
import com.github.laxika.magicalvibes.cards.c.ColossalPlow;
import com.github.laxika.magicalvibes.cards.f.FiftyFeetOfRope;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeatherArmor;
import com.github.laxika.magicalvibes.cards.m.MagistratesScepter;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.OrazcaRelic;
import com.github.laxika.magicalvibes.cards.p.PyreOfHeroes;
import com.github.laxika.magicalvibes.cards.r.RaidersKarve;
import com.github.laxika.magicalvibes.cards.r.RelicAmulet;
import com.github.laxika.magicalvibes.cards.r.ReplicatingRing;
import com.github.laxika.magicalvibes.cards.s.SpikedPitTrap;
import com.github.laxika.magicalvibes.cards.t.TreasureChest;
import com.github.laxika.magicalvibes.cards.w.WeaponRack;
import com.github.laxika.magicalvibes.cards.w.Whirlermaker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrazenBoarding.class, AdmiralBeckettBrass.class, ColossalPlow.class,
        FiftyFeetOfRope.class, FurnaceOfRath.class, Gingerbrute.class, GrizzlyBears.class,
        JaceBeleren.class, LeatherArmor.class, MagistratesScepter.class, Millstone.class, OrazcaRelic.class,
        PyreOfHeroes.class, RaidersKarve.class, RelicAmulet.class, ReplicatingRing.class,
        SpikedPitTrap.class, TreasureChest.class, WeaponRack.class, Whirlermaker.class})
class BrazenBoardingTest extends BaseCardTest {

    @Test
    void dealsFourDamageAndConjuresMatchingManaValueCardForExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBrazenBoarding(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getCard().getName()))
                .anyMatch(Set.of("Colossal Plow", "Millstone", "Relic Amulet",
                        "Pyre of Heroes")::contains);
    }

    @Test
    void conjuresAdmiralInsteadWhenExcessDamageIsAtLeastFour() {
        harness.addToBattlefield(player1, new FurnaceOfRath());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBrazenBoarding(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Admiral Beckett Brass");
    }

    @Test
    void doesNotConjureWithoutExcessDamage() {
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setToughness(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        castBrazenBoarding(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void rejectsPlayerAsTarget() {
        harness.setHand(player1, List.of(new BrazenBoarding()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void accountsForDamageAlreadyMarkedWhenChoosingSpellbookCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setMarkedDamage(1);

        castBrazenBoarding(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getName())
                .isIn("Whirlermaker", "Magistrate's Scepter", "Replicating Ring",
                        "Raiders' Karve", "Orazca Relic", "Treasure Chest");
    }

    @Test
    void conjuresThreeManaCardForThreeExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Gingerbrute());

        castBrazenBoarding(target);

        harness.assertInGraveyard(player2, "Gingerbrute");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getName())
                .isIn("Whirlermaker", "Magistrate's Scepter", "Replicating Ring",
                        "Raiders' Karve", "Orazca Relic", "Treasure Chest");
    }

    @Test
    void usesPlaneswalkerLoyaltyToDetermineExcessDamage() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new JaceBeleren());

        castBrazenBoarding(target);

        harness.assertInGraveyard(player2, "Jace Beleren");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getCard().getName())
                .isIn("Fifty Feet of Rope", "Leather Armor", "Spiked Pit Trap", "Gingerbrute");
    }

    @Test
    void doesNotConjureWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrazenBoarding()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Brazen Boarding");
    }

    private void castBrazenBoarding(Permanent target) {
        harness.setHand(player1, List.of(new BrazenBoarding()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
