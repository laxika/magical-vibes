package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrueNameNemesis.class, Shock.class, GrizzlyBears.class, Pacifism.class,
        Pyroclasm.class, WrathOfGod.class})
class TrueNameNemesisTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, the controller chooses a player and gains protection from that player")
    void choosesPlayerAndProtectsFromThatPlayer() {
        Permanent nemesis = castTrueNameNemesis();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, nemesis.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("The controller may choose themself")
    void mayChooseController() {
        Permanent nemesis = castTrueNameNemesis();
        harness.handlePermanentChosen(player1, player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, nemesis.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Choosing yourself prevents your own spells from targeting it")
    void chosenControllerCannotTargetNemesis() {
        Permanent nemesis = castTrueNameNemesis();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nemesis.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection prevents untargeted damage from the chosen player")
    void preventsChosenPlayersMassDamage() {
        Permanent nemesis = castTrueNameNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new Pyroclasm()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertOnBattlefield(player1, "True-Name Nemesis");
        assertThat(nemesis.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Protection does not prevent damage from a player who was not chosen")
    void unchosenPlayersMassDamageKillsNemesis() {
        castTrueNameNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "True-Name Nemesis");
        harness.assertInGraveyard(player1, "True-Name Nemesis");
    }

    @Test
    @DisplayName("Protection does not stop untargeted destruction by the chosen player")
    void chosenPlayersWrathDestroysNemesis() {
        castTrueNameNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0);

        harness.assertNotOnBattlefield(player1, "True-Name Nemesis");
        harness.assertInGraveyard(player1, "True-Name Nemesis");
    }

    @Test
    @DisplayName("Creatures controlled by the chosen player cannot block it")
    void chosenPlayersCreatureCannotBlock() {
        Permanent nemesis = castTrueNameNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        nemesis.setSummoningSick(false);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("It can block the chosen player's creatures and prevents their combat damage")
    void canBlockChosenPlayersCreatureWithoutTakingDamage() {
        Permanent nemesis = castTrueNameNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player1, "True-Name Nemesis");
        assertThat(nemesis.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The chosen player's Aura cannot target it")
    void chosenPlayersAuraCannotTarget() {
        Permanent nemesis = castTrueNameNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new Pacifism()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, nemesis.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    private Permanent castTrueNameNemesis() {
        harness.setHand(player1, List.of(new TrueNameNemesis()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "True-Name Nemesis");
    }
}
