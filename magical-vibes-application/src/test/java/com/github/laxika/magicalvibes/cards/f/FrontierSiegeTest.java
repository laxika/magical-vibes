package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrontierSiege.class, AirElemental.class, GrizzlyBears.class, Naturalize.class})
class FrontierSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Khans adds two green mana at both of the controller's main phases")
    void khansAddsManaAtBothMainPhases() {
        castSiege(player1, "Khans");

        advanceToPrecombatMain(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);

        advanceToPostcombatMain(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Khans does not add mana during an opponent's main phase")
    void khansDoesNotAddManaDuringOpponentsMainPhase() {
        castSiege(player1, "Khans");

        advanceToPrecombatMain(player2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Dragons may have an entering flying creature fight an opponent creature")
    void dragonsFlyingCreatureFightsOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSiege(player1, "Dragons");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castFlyingCreature(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentBears.getId()));
    }

    @Test
    @DisplayName("Dragons does not trigger for a nonflying creature")
    void dragonsDoesNotTriggerForNonflyingCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSiege(player1, "Dragons");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(opponentBears.getMarkedDamage()).isZero();
    }

    @Test
    void dragonsFightCanBeDeclinedAtResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSiege(player1, "Dragons");
        castFlyingCreature(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fightDealsDamageFromBothCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSiege(player1, "Dragons");
        castFlyingCreature(player1);
        harness.passBothPriorities();
        Permanent elemental = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AirElemental)
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(elemental.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Air Elemental");
    }

    @Test
    void khansDoesNotTriggerAFightForFlyingCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSiege(player1, "Khans");
        castFlyingCreature(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    void dragonsDoesNotAddManaAtEitherMainPhase() {
        castSiege(player1, "Dragons");
        advanceToPrecombatMain(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();

        advanceToPostcombatMain(player1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dragonsDoesNotTriggerForOpponentsFlyingCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castSiege(player1, "Dragons");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castFlyingCreature(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void khansTriggerUsesTheStackAndSurvivesSiegesDestruction() {
        castSiege(player1, "Khans");
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Frontier Siege"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Frontier Siege");
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void dragonsFightSurvivesSiegesDestruction() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castSiege(player1, "Dragons");
        castFlyingCreature(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Frontier Siege"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Frontier Siege");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Air Elemental");
    }

    private void castSiege(Player player, String mode) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new FrontierSiege(), "{3}{G}");
        harness.passBothPriorities();
        harness.handleListChoice(player, mode);
    }

    private void castFlyingCreature(Player player) {
        harness.castFromHand(player, new AirElemental(), "{3}{U}{U}");
    }

    private void advanceToPrecombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
