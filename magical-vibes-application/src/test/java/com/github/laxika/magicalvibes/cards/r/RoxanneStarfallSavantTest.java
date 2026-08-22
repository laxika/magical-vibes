package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RoxanneStarfallSavant.class)
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
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void attackingCreatesAnotherTappedMeteorite() {
        addCreatureReady(player1, new RoxanneStarfallSavant());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent meteorite = findPermanent(player1, "Meteorite");
        assertThat(meteorite.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Meteorite")).isEqualTo(1);
    }

    private void castRoxanne() {
        harness.setHand(player1, List.of(new RoxanneStarfallSavant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
