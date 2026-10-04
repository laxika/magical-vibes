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

    @Test
    @DisplayName("Targeting yourself with the red ability deals 2 damage to you")
    void burnsControllerTwiceWhenTargetingSelf() {
        addCreatureReady(player1, new GrangerGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        int life = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, life - 2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The red ability can target the Guildmage itself")
    void burnsSourceAndController() {
        Permanent guildmage = addCreatureReady(player1, new GrangerGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        int life = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, guildmage.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Granger Guildmage");
        harness.assertNotOnBattlefield(player1, "Granger Guildmage");
        harness.assertLife(player1, life - 1);
    }

    @Test
    @DisplayName("An illegal red target prevents all damage, including damage to you")
    void redAbilityDoesNotResolveWhenTargetLeaves() {
        Permanent guildmage = addCreatureReady(player1, new GrangerGuildmage());
        Permanent falcon = addCreatureReady(player2, new BayFalcon());
        harness.addMana(player1, ManaColor.RED, 1);
        int life = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, falcon.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, falcon));
        harness.passBothPriorities();

        harness.assertLife(player1, life);
        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The red ability still resolves after the Guildmage leaves")
    void redAbilityResolvesWithoutSource() {
        Permanent guildmage = addCreatureReady(player1, new GrangerGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        int life = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, guildmage));
        harness.passBothPriorities();

        harness.assertLife(player1, life - 1);
        harness.assertLife(player2, opponentLife - 1);
    }

    @Test
    @DisplayName("First strike resolves and persists after the Guildmage leaves")
    void whiteAbilityResolvesWithoutSource() {
        Permanent guildmage = addCreatureReady(player1, new GrangerGuildmage());
        Permanent falcon = addCreatureReady(player2, new BayFalcon());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, falcon.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, guildmage));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, falcon, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Activating either ability taps the Guildmage and prevents activating the other")
    void abilitiesShareTapCost() {
        Permanent guildmage = addCreatureReady(player1, new GrangerGuildmage());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, guildmage.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, guildmage, Keyword.FIRST_STRIKE)).isFalse();
    }
}
