package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AoTheDawnSky;
import com.github.laxika.magicalvibes.cards.a.AtsushiTheBlazingSky;
import com.github.laxika.magicalvibes.cards.j.JunjiTheMidnightSky;
import com.github.laxika.magicalvibes.cards.k.KamiOfTransience;
import com.github.laxika.magicalvibes.cards.k.KuraTheBoundlessSky;
import com.github.laxika.magicalvibes.cards.m.MoonsnarePrototype;
import com.github.laxika.magicalvibes.cards.r.RoaringEarth;
import com.github.laxika.magicalvibes.cards.t.TamiyoCompleatedSage;
import com.github.laxika.magicalvibes.model.CounterType;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OtawaraSoaringCity.class, Forest.class, GrizzlyBears.class, Spellbook.class,
        AoTheDawnSky.class, AtsushiTheBlazingSky.class, JunjiTheMidnightSky.class,
        KamiOfTransience.class, KuraTheBoundlessSky.class, MoonsnarePrototype.class,
        RoaringEarth.class, TamiyoCompleatedSage.class})
class OtawaraSoaringCityTest extends BaseCardTest {

    @Test
    @DisplayName("Adds blue mana")
    void addsBlueMana() {
        harness.addToBattlefield(player1, new OtawaraSoaringCity());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel returns an artifact and is reduced by each legendary creature")
    void channelReturnsArtifactWithLegendaryCostReduction() {
        GrizzlyBears legendaryCreature = new GrizzlyBears();
        legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        harness.addToBattlefield(player1, legendaryCreature);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player2, "Spellbook");
        harness.assertInGraveyard(player1, "Otawara, Soaring City");
    }

    @Test
    @DisplayName("Channel cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, creature, enchantment, or planeswalker");
    }

    @Test
    void channelPaysFullCostAndDiscardsBeforeResolving() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoonsnarePrototype());
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, target.getId());

        harness.assertNotInHand(player1, "Otawara, Soaring City");
        harness.assertInGraveyard(player1, "Otawara, Soaring City");
        harness.assertOnBattlefield(player2, "Moonsnare Prototype");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Moonsnare Prototype");
        harness.assertInHand(player2, "Moonsnare Prototype");
    }

    @Test
    void channelReturnsCreatureToOwnerRatherThanController() {
        KamiOfTransience creature = new KamiOfTransience();
        creature.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kami of Transience");
        harness.assertInHand(player1, "Kami of Transience");
        harness.assertNotInHand(player2, "Kami of Transience");
    }

    @Test
    void channelCanReturnOwnEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RoaringEarth());
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Roaring Earth");
        harness.assertInHand(player1, "Roaring Earth");
    }

    @Test
    void channelReturnsPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TamiyoCompleatedSage());
        target.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tamiyo, Compleated Sage");
        harness.assertInHand(player2, "Tamiyo, Compleated Sage");
    }

    @Test
    void twoLegendaryCreaturesReduceCostByTwo() {
        harness.addToBattlefield(player1, new AoTheDawnSky());
        harness.addToBattlefield(player1, new AtsushiTheBlazingSky());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoonsnarePrototype());
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Moonsnare Prototype");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void reductionCannotRemoveBlueManaRequirement() {
        harness.addToBattlefield(player1, new AoTheDawnSky());
        harness.addToBattlefield(player1, new AtsushiTheBlazingSky());
        harness.addToBattlefield(player1, new JunjiTheMidnightSky());
        harness.addToBattlefield(player1, new KuraTheBoundlessSky());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoonsnarePrototype());
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Otawara, Soaring City");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Moonsnare Prototype");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void nonlegendaryCreaturesLegendaryNoncreaturesAndOpposingLegendsDoNotReduceCost() {
        harness.addToBattlefield(player1, new KamiOfTransience());
        harness.addToBattlefield(player1, new OtawaraSoaringCity());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new TamiyoCompleatedSage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.addToBattlefield(player2, new AoTheDawnSky());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoonsnarePrototype());
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Otawara, Soaring City");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Moonsnare Prototype");
    }

    @Test
    void channelRequiresTargetAndDoesNotDiscardOnIllegalActivation() {
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Otawara, Soaring City");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void channelStillCostsDiscardWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoonsnarePrototype());
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.setHand(player2, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.activateHandAbility(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Moonsnare Prototype");
        harness.assertNotOnBattlefield(player2, "Moonsnare Prototype");
        harness.assertInGraveyard(player1, "Otawara, Soaring City");
        harness.assertInGraveyard(player2, "Otawara, Soaring City");
        assertThat(gd.stack).isEmpty();
    }

}
