package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DomriAnarchOfBolas.class, Cancel.class, GrizzlyBears.class, SerraAngel.class})
class DomriAnarchOfBolasTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+0")
    void boostsOwnCreatures() {
        addReadyDomri(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBear = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBear = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("+1 adds a chosen red or green mana and makes creature spells uncounterable this turn")
    void plusOneAddsManaAndMakesCreatureSpellsUncounterableThisTurn() {
        Permanent domri = addReadyDomri(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(firstBears, secondBears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new Cancel(), new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firstBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, secondBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .count()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Cancel"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("-2 makes a creature you control fight a creature an opponent controls")
    void minusTwoFights() {
        Permanent domri = addReadyDomri(player1);
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(angelId, bearsId));
        harness.passBothPriorities();

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(findPermanent(player1, "Serra Angel").getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-2 rejects an opponent creature as its first target")
    void minusTwoFirstTargetMustBeControlled() {
        addReadyDomri(player1);
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(bearsId, angelId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDomri(Player player) {
        Permanent perm = new Permanent(new DomriAnarchOfBolas());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
