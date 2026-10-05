package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoldenDemise;
import com.github.laxika.magicalvibes.cards.i.Impale;
import com.github.laxika.magicalvibes.cards.m.MartyrOfDusk;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PitilessPlunderer.class, GrizzlyBears.class, Shock.class,
        GoldenDemise.class, Impale.class, MartyrOfDusk.class, RaptorCompanion.class})
class PitilessPlundererTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure token when another creature you control dies")
    void createsTreasureWhenAnotherCreatureYouControlDies() {
        harness.addToBattlefield(player1, new PitilessPlunderer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Treasure token when an opponent's creature dies")
    void doesNotCreateTreasureWhenOpponentsCreatureDies() {
        harness.addToBattlefield(player1, new PitilessPlunderer());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Does not create Treasure for its own death")
    void doesNotTriggerForItsOwnDeath() {
        harness.addToBattlefield(player1, new PitilessPlunderer());
        harness.setHand(player1, List.of(new Impale()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0,
                harness.getPermanentId(player1, "Pitiless Plunderer"));

        harness.assertInGraveyard(player1, "Pitiless Plunderer");
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Creates one Treasure for each allied creature dying simultaneously")
    void triggersSeparatelyForSimultaneousDeaths() {
        harness.addToBattlefield(player1, new PitilessPlunderer());
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.addToBattlefield(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new GoldenDemise()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Still triggers when Plunderer dies simultaneously with another allied creature")
    void triggersWhenDyingWithAnotherCreature() {
        var plunderer = harness.addToBattlefieldAndReturn(player1, new PitilessPlunderer());
        var companion = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        plunderer.setMarkedDamage(4);
        companion.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Pitiless Plunderer");
        harness.assertInGraveyard(player1, "Raptor Companion");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Creates Treasure when an allied creature token dies")
    void triggersForCreatureTokenDeath() {
        harness.addToBattlefield(player1, new PitilessPlunderer());
        var martyr = harness.addToBattlefieldAndReturn(player1, new MartyrOfDusk());
        martyr.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        var vampire = findPermanent(player1, "Vampire");
        vampire.setMarkedDamage(1);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vampire")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }
}
