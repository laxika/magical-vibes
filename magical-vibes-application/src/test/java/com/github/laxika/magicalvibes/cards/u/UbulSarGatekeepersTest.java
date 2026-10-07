package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UbulSarGatekeepers.class, AzoriusGuildgate.class, BorosGuildgate.class, KraulWarrior.class})
class UbulSarGatekeepersTest extends BaseCardTest {

    @Test
    @DisplayName("With two Gates, the chosen opponent creature gets -2/-2 and dies")
    void twoGatesShrinksOpponentCreature() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        harness.addToBattlefield(player2, new KraulWarrior());

        UUID warriorId = harness.getPermanentId(player2, "Kraul Warrior");
        castGatekeepers();
        harness.passBothPriorities(); // resolve creature spell — trigger-time target prompt
        harness.handlePermanentChosen(player1, warriorId);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertNotOnBattlefield(player2, "Kraul Warrior");
    }

    @Test
    @DisplayName("Trigger prompt only offers creatures an opponent controls")
    void promptOffersOnlyOpponentCreatures() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.addToBattlefield(player2, new KraulWarrior());

        UUID opponentWarriorId = harness.getPermanentId(player2, "Kraul Warrior");
        castGatekeepers();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(opponentWarriorId);
    }

    @Test
    @DisplayName("With only one Gate the trigger does not fire and no target is chosen")
    void oneGateDoesNotTrigger() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player2, new KraulWarrior());

        castGatekeepers();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Kraul Warrior");
        harness.assertOnBattlefield(player1, "Ubul Sar Gatekeepers");
    }

    @Test
    @DisplayName("Gates an opponent controls do not count")
    void opponentGatesDoNotCount() {
        setUpTurn();
        harness.addToBattlefield(player2, new AzoriusGuildgate());
        harness.addToBattlefield(player2, new BorosGuildgate());
        harness.addToBattlefield(player2, new KraulWarrior());

        castGatekeepers();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Kraul Warrior");
    }

    @Test
    @DisplayName("ETB fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        harness.addToBattlefield(player2, new KraulWarrior());

        UUID warriorId = harness.getPermanentId(player2, "Kraul Warrior");
        castGatekeepers();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, warriorId);

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Gate condition is checked again when the trigger resolves")
    void losingGateBeforeResolutionPreventsShrink() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        var gate = harness.addToBattlefieldAndReturn(player1, new BorosGuildgate());
        var target = harness.addToBattlefieldAndReturn(player2, new UbulSarGatekeepers());

        castGatekeepers();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(gate);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The shrink lasts through the end step and expires during cleanup")
    void shrinkExpiresAtEndOfTurn() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        var target = harness.addToBattlefieldAndReturn(player2, new UbulSarGatekeepers());

        castGatekeepers();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Gates with the same name satisfy the condition")
    void sameNamedGatesCountSeparately() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        var target = harness.addToBattlefieldAndReturn(player2, new UbulSarGatekeepers());

        castGatekeepers();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("With no opposing creatures no target or trigger remains pending")
    void noLegalTargets() {
        setUpTurn();
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());

        castGatekeepers();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Ubul Sar Gatekeepers");
    }

    private void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void castGatekeepers() {
        harness.setHand(player1, List.of(new UbulSarGatekeepers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
