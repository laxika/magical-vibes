package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshenmoorGouger;
import com.github.laxika.magicalvibes.cards.c.Cinderbones;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmolderInitiate.class, AshenmoorGouger.class, Cinderbones.class, SafeholdSentry.class})
class SmolderInitiateTest extends BaseCardTest {

    private void castBlackSpell(Player caster) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castFromHand(caster, new Cinderbones(), "{2}{B}");
    }

    @Test
    @DisplayName("A player casting a black spell prompts the controller's may-pay ability")
    void blackSpellTriggersMayPay() {
        harness.addToBattlefield(player1, new SmolderInitiate());
        castBlackSpell(player1);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A multicolored black spell also prompts the controller's may-pay ability")
    void multicoloredBlackSpellTriggersMayPay() {
        harness.addToBattlefield(player1, new SmolderInitiate());
        harness.setHand(player1, List.of(new AshenmoorGouger()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Paying {1} makes the chosen target player lose 1 life")
    void payMakesTargetLoseLife() {
        harness.addToBattlefield(player1, new SmolderInitiate());

        // Opponent casts the black spell so player1's payment mana stays isolated.
        castBlackSpell(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("The controller may target themselves")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new SmolderInitiate());

        castBlackSpell(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Declining leaves life totals unchanged")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new SmolderInitiate());
        castBlackSpell(player1);
        harness.setLife(player2, 20);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Casting a non-black spell does not trigger the ability")
    void nonBlackSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SmolderInitiate());
        harness.castFromHand(player1, new SafeholdSentry(), "{1}{W}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Payment mana is retained until the targeted trigger resolves")
    void paymentWaitsForResolution() {
        harness.addToBattlefield(player1, new SmolderInitiate());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        castBlackSpell(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A black creature spell does not trigger its own Smolder Initiate ability")
    void doesNotTriggerFromItsOwnCast() {
        harness.castFromHand(player1, new SmolderInitiate(), "{B}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

}
