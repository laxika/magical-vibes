package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SawhornNemesis.class, Shock.class, GrizzlyBears.class, HillGiant.class, TurnToFrog.class})
class SawhornNemesisTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, Sawhorn Nemesis chooses a player")
    void choosesPlayerOnEntry() {
        Permanent nemesis = castSawhornNemesis();

        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(nemesis.getRememberedTargetPlayerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Doubles damage dealt to the chosen player")
    void doublesDamageToChosenPlayer() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubles damage dealt to a permanent controlled by the chosen player")
    void doublesDamageToChosenPlayersPermanent() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not double damage dealt to another player")
    void doesNotDoubleDamageToAnotherPlayer() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubled Shock kills a creature that survives ordinary Shock")
    void doublesLethalDamageToThreeToughnessCreature() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, giant.getId());

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Does not double damage to another player's permanent")
    void doesNotDoubleDamageToAnotherPlayersPermanent() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, giant.getId());

        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("May choose its controller and doubles damage to itself")
    void canChooseControllerAndDoubleDamageToItself() {
        Permanent nemesis = castSawhornNemesis();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, nemesis.getId());

        harness.assertInGraveyard(player1, "Sawhorn Nemesis");
    }

    @Test
    @DisplayName("Doubles damage from a source controlled by the chosen player")
    void doublesDamageFromChosenPlayersSource() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Two Nemeses choosing the same player quadruple damage")
    void multipleNemesesMultiplyDamage() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Doubles combat damage to the chosen player")
    void doublesCombatDamage() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Stops doubling damage after leaving the battlefield")
    void leavingBattlefieldStopsDamageDoubling() {
        Permanent nemesis = castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, nemesis.getId());
        harness.castAndResolveInstant(player1, 0, nemesis.getId());
        harness.assertInGraveyard(player1, "Sawhorn Nemesis");
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("May choose its controller and double damage to that player")
    void doublesDamageToChosenController() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Stops doubling damage after losing all abilities")
    void losingAbilitiesStopsDamageDoubling() {
        Permanent nemesis = castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, nemesis.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private Permanent castSawhornNemesis() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new SawhornNemesis(), "{3}{R}");
        harness.passBothPriorities();
        return findPermanent(player1, "Sawhorn Nemesis");
    }
}
