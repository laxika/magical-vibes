package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Bloodbriar;
import com.github.laxika.magicalvibes.cards.a.AetherworksMarvel;
import com.github.laxika.magicalvibes.cards.c.CauldronFamiliar;
import com.github.laxika.magicalvibes.cards.d.DuskmantleGuildmage;
import com.github.laxika.magicalvibes.cards.d.DyingWish;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lumberknot;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.SyrKonradTheGrim;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hushbringer.class, Bloodbriar.class, DiabolicEdict.class, GrizzlyBears.class,
        Lumberknot.class, Shock.class, SoulWarden.class, AetherworksMarvel.class,
        CauldronFamiliar.class, DuskmantleGuildmage.class, DyingWish.class, Frogify.class,
        Gingerbrute.class, SyrKonradTheGrim.class})
class HushbringerTest extends BaseCardTest {

    @Test
    void suppressesCreatureEnteringTriggers() {
        harness.addToBattlefield(player1, new Hushbringer());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void suppressesCreatureDeathTriggers() {
        harness.addToBattlefield(player1, new Hushbringer());
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player1, new Lumberknot());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);

        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotSuppressSacrificeTriggers() {
        harness.addToBattlefield(player1, new Hushbringer());
        Permanent bloodbriar = harness.addToBattlefieldAndReturn(player1, new Bloodbriar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void suppressesItsOwnArrivalForBothPlayers() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player2, new SoulWarden());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new Hushbringer(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hushbringer");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void suppressesAnOpponentsOwnCreatureEntryAbility() {
        harness.addToBattlefield(player1, new Hushbringer());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new CauldronFamiliar(), "{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cauldron Familiar");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void suppressesItsOwnDeathAndAllowsLaterDeathsToTrigger() {
        Permanent hushbringer = harness.addToBattlefieldAndReturn(player1, new Hushbringer());
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player2, new Lumberknot());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, hushbringer.getId());

        harness.assertInGraveyard(player1, "Hushbringer");
        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void suppressesSimultaneousDeathsIncludingItsOwn() {
        Permanent hushbringer = harness.addToBattlefieldAndReturn(player1, new Hushbringer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player2, new Lumberknot());

        hushbringer.setMarkedDamage(2);
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Hushbringer");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotSuppressSimultaneousDeathsWhenItHasLostItsAbilities() {
        Permanent hushbringer = harness.addToBattlefieldAndReturn(player1, new Hushbringer());
        Permanent frogify = harness.addToBattlefieldAndReturn(player1, new Frogify());
        frogify.setAttachedTo(hushbringer.getId());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent lumberknot = harness.addToBattlefieldAndReturn(player2, new Lumberknot());

        hushbringer.setMarkedDamage(1);
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hushbringer");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(lumberknot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void suppressesEnchantedCreatureDeathTriggers() {
        harness.addToBattlefield(player1, new Hushbringer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent wish = harness.addToBattlefieldAndReturn(player1, new DyingWish());
        wish.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    void suppressesPermanentDeathTriggersForCreatures() {
        harness.addToBattlefield(player1, new Hushbringer());
        harness.addToBattlefield(player1, new AetherworksMarvel());
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        gd.playerEnergyCounters.put(player1.getId(), 0);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, brute.getId());

        harness.assertInGraveyard(player1, "Gingerbrute");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void suppressesDelayedFromAnywhereTriggersCausedByCreatureDeaths() {
        harness.addToBattlefield(player1, new Hushbringer());
        harness.addToBattlefield(player1, new DuskmantleGuildmage());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void suppressesGraveyardDepartureTriggersWhenACreatureReturnsToBattlefield() {
        harness.addToBattlefield(player1, new Hushbringer());
        harness.addToBattlefield(player1, new SyrKonradTheGrim());
        harness.addToBattlefield(player1, new Gingerbrute());
        harness.setGraveyard(player1, List.of(new CauldronFamiliar()));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gingerbrute");
        harness.assertOnBattlefield(player1, "Cauldron Familiar");
        harness.assertNotInGraveyard(player1, "Cauldron Familiar");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
