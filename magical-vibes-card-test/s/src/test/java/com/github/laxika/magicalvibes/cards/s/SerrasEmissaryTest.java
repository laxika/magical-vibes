package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerrasEmissary.class, GrizzlyBears.class, Shock.class})
class SerrasEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("As Serra's Emissary enters, it chooses a card type")
    void choosesCardTypeAsItEnters() {
        harness.setHand(player1, List.of(new SerrasEmissary()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "INSTANT");

        assertThat(findPermanent(player1, "Serra's Emissary").getChosenCardType())
                .isEqualTo(CardType.INSTANT);
    }

    @Test
    @DisplayName("You and your creatures have protection from the chosen card type")
    void protectsControllerAndOwnCreatures() {
        addReadyEmissary(player1, CardType.INSTANT);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Instant protection includes the Emissary itself and spells you control")
    void protectsItselfFromOwnInstants() {
        Permanent emissary = addReadyEmissary(player1, CardType.INSTANT);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, emissary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection does not extend to opponents or their creatures")
    void doesNotProtectOpponents() {
        addReadyEmissary(player1, CardType.INSTANT);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature protection does not stop damage from instant spells")
    void differentCardTypeStillDealsDamage() {
        addReadyEmissary(player1, CardType.CREATURE);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 18);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature protection prevents combat damage to the controller")
    void preventsCombatDamageToController() {
        harness.castFromHand(player1, new SerrasEmissary(), "{4}{W}{W}{W}");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Serra's Emissary");
        harness.handleListChoice(player1, "CREATURE");
        harness.assertOnBattlefield(player1, "Serra's Emissary");
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Creature protection prevents damage to a creature blocking an attacker")
    void preventsCombatDamageToOwnCreature() {
        addReadyEmissary(player1, CardType.CREATURE);
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures cannot block a creature protected from creatures")
    void preventsCreatureBlocking() {
        addReadyEmissary(player1, CardType.CREATURE);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection ends when the Emissary leaves the battlefield")
    void protectionEndsWhenSourceLeaves() {
        Permanent emissary = addReadyEmissary(player1, CardType.INSTANT);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, emissary));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.assertLife(player1, 18);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addReadyEmissary(Player controller, CardType chosenType) {
        Permanent emissary = addCreatureReady(controller, new SerrasEmissary());
        emissary.setChosenCardType(chosenType);
        return emissary;
    }
}
