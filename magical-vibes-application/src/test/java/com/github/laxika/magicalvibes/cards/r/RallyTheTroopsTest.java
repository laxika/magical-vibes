package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlertShuInfantry;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PortalMage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RallyTheTroops.class, AlertShuInfantry.class, Plains.class, NicolBolasPlaneswalker.class,
        PortalMage.class})
class RallyTheTroopsTest extends BaseCardTest {

    @Test
    @DisplayName("Cast during declare attackers while attacked: untaps all your creatures")
    void untapsControlledCreaturesWhenAttacked() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent tapped1 = tappedCreature(player2);
        Permanent tapped2 = tappedCreature(player2);
        harness.setHand(player2, List.of(new RallyTheTroops()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(tapped1.isTapped()).isFalse();
        assertThat(tapped2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps only creatures you control")
    void untapsOnlyCreaturesYouControl() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent ownCreature = tappedCreature(player2);
        Permanent opponentCreature = tappedCreature(player1);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        ownLand.tap();
        harness.setHand(player2, List.of(new RallyTheTroops()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castAndResolveInstant(player2, 0);

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(ownLand.isTapped()).isTrue();
    }

    @Test
    @CardUsed(NicolBolasPlaneswalker.class)
    @DisplayName("Cannot cast when only a planeswalker you control is attacked")
    void cannotCastWhenOnlyPlaneswalkerIsAttacked() {
        harness.forceActivePlayer(player1);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        Permanent attacker = addAttackerTargeting(player1, player2);
        attacker.setAttackTarget(planeswalker.getId());
        harness.setHand(player2, List.of(new RallyTheTroops()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast during declare attackers if not attacked")
    void cannotCastWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        tappedCreature(player2);
        harness.setHand(player2, List.of(new RallyTheTroops()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast outside the declare attackers step")
    void cannotCastOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        tappedCreature(player2);
        harness.setHand(player2, List.of(new RallyTheTroops()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast after the declared attacker leaves combat")
    void canCastAfterAttackerLeavesCombat() {
        Permanent attacker = addCreatureReady(player1, new AlertShuInfantry());
        Permanent defender = tappedCreature(player2);
        harness.setHand(player2, List.of(new RallyTheTroops()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);

        harness.castAndResolveInstant(player2, 0);

        assertThat(defender.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Rally the Troops");
    }

    @Test
    @DisplayName("Can cast with no creatures to untap")
    void canCastWithNoControlledCreatures() {
        addCreatureReady(player1, new AlertShuInfantry());
        harness.setHand(player2, List.of(new RallyTheTroops()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Rally the Troops");
    }

    @Test
    @CardUsed({NicolBolasPlaneswalker.class, PortalMage.class})
    @DisplayName("Redirecting an attacker from your planeswalker to you does not count as being attacked")
    void cannotCastAfterAttackerIsRedirectedFromPlaneswalkerToPlayer() {
        Permanent attacker = addCreatureReady(player1, new AlertShuInfantry());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        harness.setHand(player1, List.of(new PortalMage()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new RallyTheTroops()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
            harness.castCreature(player1, 0, attacker.getId());
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, attacker.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, player2.getId());
        });

        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(attacker.getAttackTarget()).isEqualTo(player2.getId());
        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent addAttackerTargeting(Player attackerController, Player defender) {
        Permanent perm = addCreatureReady(attackerController, new AlertShuInfantry());
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent tappedCreature(Player player) {
        Permanent perm = addCreatureReady(player, new AlertShuInfantry());
        perm.tap();
        return perm;
    }
}
