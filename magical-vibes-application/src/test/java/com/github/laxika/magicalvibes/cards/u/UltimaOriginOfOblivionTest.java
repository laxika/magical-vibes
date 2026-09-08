package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({UltimaOriginOfOblivion.class, Forest.class})
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

    private Permanent castUltima() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new UltimaOriginOfOblivion()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Ultima, Origin of Oblivion");
    }
}
