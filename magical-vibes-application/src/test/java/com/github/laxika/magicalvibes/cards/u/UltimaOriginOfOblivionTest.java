package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StartingTown;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UltimaOriginOfOblivion.class, Forest.class, StartingTown.class})
class UltimaOriginOfOblivionTest extends BaseCardTest {

    @Test
    @DisplayName("blight counter removes land types and abilities, then the land taps for two colorless")
    void blightedLandTapsForTwoColorless() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ultima = castUltima();
        ultima.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ultima)));
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.getCounterCount(CounterType.BLIGHT)).isOne();
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).isEmpty();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("the blight rule persists after Ultima leaves and ends when the counter is removed")
    void blightRuleIsSourceIndependentAndCounterBound() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ultima = castUltima();
        ultima.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ultima)));
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(ultima);
        gd.expireFloatingEffectsForDepartedSource(ultima.getId());
        gd.playerManaPools.get(player1.getId()).clear();
        forest.untap();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isOne();

        forest.setCounterCount(CounterType.BLIGHT, 0);
        forest.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A blight counter not placed by the attack ability has no inherent effect")
    void unrelatedBlightCounterDoesNotChangeLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setCounterCount(CounterType.BLIGHT, 1);
        castUltima();

        gd.playerManaPools.get(player1.getId()).clear();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Removing every blight counter permanently ends the resolved attack's effect")
    void blightEffectDoesNotResumeWhenCounterReturns() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ultima = castUltima();
        ultima.setSummoningSick(false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ultima)));
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        forest.setCounterCount(CounterType.BLIGHT, 0);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();

        forest.untap();
        gd.playerManaPools.get(player1.getId()).clear();
        forest.setCounterCount(CounterType.BLIGHT, 1);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("An attack creates the blight effect even without an entry trigger")
    void attackEstablishesBlightEffectWithoutEntryTrigger() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        addCreatureReady(player1, new UltimaOriginOfOblivion());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A printed activated land ability producing colorless gets one additional mana immediately")
    void printedActivatedManaAbilityGetsBonus() {
        harness.addToBattlefield(player1, new StartingTown());
        castUltima();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Producing colored mana does not get the bonus")
    void coloredManaDoesNotGetBonus() {
        harness.addToBattlefield(player1, new StartingTown());
        castUltima();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isOne();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("An opponent's blighted land produces only one colorless mana")
    void opponentDoesNotGetManaBonus() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ultima = castUltima();
        ultima.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isOne();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent castUltima() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new UltimaOriginOfOblivion(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Ultima, Origin of Oblivion");
    }
}
