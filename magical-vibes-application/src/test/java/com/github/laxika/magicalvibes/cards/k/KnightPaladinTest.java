package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightPaladin.class, GrizzlyBears.class})
class KnightPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to each opponent when it enters")
    void dealsFourDamageToEachOpponentOnEntry() {
        harness.castFromHand(player1, new KnightPaladin(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Crew 1 animates Knight Paladin and taps the crew member")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent vehicle = addCreatureReady(player1, new KnightPaladin());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entry damage still resolves after Knight Paladin leaves the battlefield")
    void entryDamageResolvesWithoutSource() {
        harness.castFromHand(player1, new KnightPaladin(), "{5}");
        harness.passBothPriorities();
        Permanent vehicle = findPermanent(player1, "Knight Paladin");
        gd.playerBattlefields.get(player1.getId()).remove(vehicle);
        gd.playerGraveyards.get(player1.getId()).add(vehicle.getCard());

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew and animation expires after the turn")
    void summoningSickCrewAndTemporaryAnimation() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new KnightPaladin());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapped creatures cannot pay the crew cost")
    void tappedCreatureCannotCrew() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new KnightPaladin());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
