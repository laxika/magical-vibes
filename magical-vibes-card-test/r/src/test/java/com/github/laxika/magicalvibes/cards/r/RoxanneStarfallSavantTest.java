package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoldRush;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoxanneStarfallSavant.class, GoldRush.class})
class RoxanneStarfallSavantTest extends BaseCardTest {

    @Test
    void entersWithATappedMeteoriteThatDealsTwoDamage() {
        castRoxanne();

        Permanent meteorite = findPermanent(player1, "Meteorite");
        assertThat(meteorite.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void tappingMeteoriteForManaAddsAnotherManaOfTheChosenColor() {
        castRoxanne();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        Permanent meteorite = findPermanent(player1, "Meteorite");
        meteorite.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(meteorite), 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void attackingCreatesAnotherTappedMeteorite() {
        addCreatureReady(player1, new RoxanneStarfallSavant());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent meteorite = findPermanent(player1, "Meteorite");
        assertThat(meteorite.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Meteorite")).isEqualTo(1);
    }

    @Test
    void meteoriteCanDealDamageToACreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RoxanneStarfallSavant());
        castRoxanne();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Roxanne, Starfall Savant");
    }

    @Test
    void meteoriteProducesOnlyOneManaAfterRoxanneLeaves() {
        castRoxanne();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        Permanent roxanne = findPermanent(player1, "Roxanne, Starfall Savant");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, roxanne));

        Permanent meteorite = findPermanent(player1, "Meteorite");
        meteorite.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(meteorite), 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void tappingATreasureAddsAnotherManaOfItsChosenColor() {
        harness.addToBattlefield(player1, new RoxanneStarfallSavant());
        harness.setHand(player1, List.of(new GoldRush()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);

        Permanent treasure = findPermanent(player1, "Treasure");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(treasure), 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void opponentsTreasureDoesNotReceiveRoxannesManaBonus() {
        harness.addToBattlefield(player1, new RoxanneStarfallSavant());
        harness.setHand(player2, List.of(new GoldRush()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0);

        Permanent treasure = findPermanent(player2, "Treasure");
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(treasure), 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    private void castRoxanne() {
        harness.setHand(player1, List.of(new RoxanneStarfallSavant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
