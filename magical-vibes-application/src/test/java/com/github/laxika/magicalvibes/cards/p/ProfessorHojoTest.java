package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.IcatianPriest;
import com.github.laxika.magicalvibes.cards.i.IcatianInfantry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfessorHojo.class, IcatianPriest.class, IcatianInfantry.class})
class ProfessorHojoTest extends BaseCardTest {

    @Test
    void reducesFirstTargetedAbilityAndDrawsOncePerTurn() {
        addCreatureReady(player1, new ProfessorHojo());
        addCreatureReady(player1, new IcatianPriest());
        Permanent target = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.WHITE, 5);
        GameData gameData = harness.getGameData();
        int handSize = gameData.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, target.getId());

        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gameData.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSize + 1);

        harness.activateAbility(player1, 1, null, target.getId());

        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void reductionRequiresTargetingYourCreature() {
        addCreatureReady(player1, new ProfessorHojo());
        addCreatureReady(player1, new IcatianPriest());
        Permanent opponentTarget = addCreatureReady(player2, new IcatianInfantry());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, opponentTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void reductionDoesNotApplyDuringAnOpponentsTurn() {
        addCreatureReady(player1, new ProfessorHojo());
        addCreatureReady(player1, new IcatianPriest());
        Permanent target = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.WHITE, 2);
        gd.activePlayerId = player2.getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
