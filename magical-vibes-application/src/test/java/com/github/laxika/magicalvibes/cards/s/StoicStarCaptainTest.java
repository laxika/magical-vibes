package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExtinguisherBattleship;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IrontreadCrusher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoicStarCaptain.class, IrontreadCrusher.class, ExtinguisherBattleship.class, GrizzlyBears.class})
class StoicStarCaptainTest extends BaseCardTest {

    @Test
    void creaturesYouControlCrewVehiclesAsThoughTheirPowerWereTwoGreater() {
        Permanent captain = addReady(player1, new StoicStarCaptain());
        Permanent vehicle = addReady(player1, new IrontreadCrusher());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(captain.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    void creaturesYouControlStationAsThoughTheirPowerWereTwoGreater() {
        addReady(player1, new StoicStarCaptain());
        Permanent spacecraft = addReady(player1, new ExtinguisherBattleship());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spacecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void seeksARandomMatchingSpacecraftIntoHand() {
        Permanent captain = addReady(player1, new StoicStarCaptain());
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        ExtinguisherBattleship spacecraft = new ExtinguisherBattleship();
        harness.setLibrary(player1, List.of(grizzlyBears, spacecraft));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(captain.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(spacecraft);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(grizzlyBears);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
