package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SunhomeGuildmageTest extends BaseCardTest {

    private void addMana(Player player, int generic) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, generic);
    }

    @Test
    @DisplayName("First ability gives +1/+0 to creatures you control only")
    void boostsOwnCreaturesOnly() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SunhomeGuildmage());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent oppBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        addMana(player1, 1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectivePower()).isEqualTo(3);
        assertThat(ownBears.getEffectivePower()).isEqualTo(3);
        assertThat(ownBears.getEffectiveToughness()).isEqualTo(2);
        assertThat(oppBears.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The +1/+0 boost wears off at end of turn")
    void boostWearsOff() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SunhomeGuildmage());

        harness.forceActivePlayer(player1);
        addMana(player1, 1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);
        harness.passBothPriorities();
        assertThat(source.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(0);
        assertThat(source.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Second ability creates a 1/1 Soldier token with haste")
    void createsHastySoldierToken() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SunhomeGuildmage());

        harness.forceActivePlayer(player1);
        addMana(player1, 2);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p != source)
                .findFirst()
                .orElseThrow();

        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
    }
}
