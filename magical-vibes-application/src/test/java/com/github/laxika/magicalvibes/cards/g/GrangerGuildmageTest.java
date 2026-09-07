package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrangerGuildmage.class, BayFalcon.class, Forest.class})
class GrangerGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("{R}, {T}: deals 1 damage to a player and 1 damage to you")
    void burnsPlayerAndController() {
        Permanent guildmage = addCreatureReady(player1, new GrangerGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife - 1);
    }

    @Test
    @DisplayName("{R}, {T}: deals 1 damage to a creature and 1 damage to you")
    void burnsCreatureAndController() {
        Permanent guildmage = addCreatureReady(player1, new GrangerGuildmage());
        Permanent falcon = addCreatureReady(player2, new BayFalcon());
        harness.addMana(player1, ManaColor.RED, 1);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, falcon.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(falcon.getCard().getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife - 1);
    }

    @Test
    @DisplayName("The red ability can target a creature its controller controls")
    void burnsOwnCreatureAndController() {
        Permanent guildmage = addCreatureReady(player1, new GrangerGuildmage());
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        harness.addMana(player1, ManaColor.RED, 1);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, falcon.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(falcon.getCard().getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife - 1);
    }

    @Test
    @DisplayName("{W}, {T}: target creature gains first strike until end of turn")
    void grantsFirstStrike() {
        Permanent guildmage = addCreatureReady(player1, new GrangerGuildmage());
        Permanent falcon = addCreatureReady(player2, new BayFalcon());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, falcon.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gqs.hasKeyword(gd, falcon, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent guildmage = addCreatureReady(player1, new GrangerGuildmage());
        Permanent falcon = addCreatureReady(player1, new BayFalcon());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, falcon.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The white ability cannot target a noncreature permanent")
    void whiteAbilityRejectsNonCreaturePermanent() {
        addCreatureReady(player1, new GrangerGuildmage());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
