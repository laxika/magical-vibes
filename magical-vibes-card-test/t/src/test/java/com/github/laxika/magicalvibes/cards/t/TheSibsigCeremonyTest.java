package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CallOfTheConclave;
import com.github.laxika.magicalvibes.cards.d.DalkovanPackbeasts;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GatherSpecimens;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheSibsigCeremony.class, CallOfTheConclave.class, GrizzlyBears.class,
        GatherSpecimens.class, Unsummon.class, DalkovanPackbeasts.class})
class TheSibsigCeremonyTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells cost {2} less and cast creatures are destroyed for a Zombie Druid")
    void reducesCreatureSpellsAndDestroysCastCreaturesForToken() {
        harness.addToBattlefield(player1, new TheSibsigCeremony());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ZOMBIE))
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.DRUID))
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature token entering without being cast does not trigger the ability")
    void uncastCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheSibsigCeremony());
        harness.setHand(player1, List.of(new CallOfTheConclave()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.CENTAUR)))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ZOMBIE)))
                .isEmpty();
    }

    @Test
    @DisplayName("A nontoken creature put onto the battlefield without casting is retained")
    void uncastNontokenCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheSibsigCeremony());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Zombie Druid");
    }

    @Test
    @DisplayName("Both generic mana are removed from a creature's cost")
    void reducesCreatureCostByTwoGenericMana() {
        harness.addToBattlefield(player1, new TheSibsigCeremony());
        harness.setHand(player1, List.of(new DalkovanPackbeasts()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dalkovan Packbeasts");
        harness.assertOnBattlefield(player1, "Zombie Druid");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Zombie is created even when the entering creature is returned to hand in response")
    void createsTokenAfterEnteringCreatureLeaves() {
        harness.addToBattlefield(player1, new TheSibsigCeremony());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Zombie Druid");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's cast creature is unaffected and pays its full cost")
    void opponentsCreatureIsUnaffected() {
        harness.addToBattlefield(player1, new TheSibsigCeremony());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertNotOnBattlefield(player1, "Zombie Druid");
    }

    @Test
    @DisplayName("An opponent's creature redirected by Gather Specimens was not cast by you")
    void redirectedOpponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new TheSibsigCeremony());
        harness.setHand(player1, List.of(new GatherSpecimens()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castAndResolveInstant(player1, 0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Zombie Druid");
    }
}
