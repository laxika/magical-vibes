package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sparkcaster.class, AlphaKavu.class, SlingshotGoblin.class, StormscapeFamiliar.class, JaceBeleren.class})
class SparkcasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only red or green creatures you control, including itself")
    void etbOffersOnlyRedOrGreenCreaturesYouControl() {
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new AlphaKavu()).getId();
        UUID redId = harness.addToBattlefieldAndReturn(player1, new SlingshotGoblin()).getId();
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new StormscapeFamiliar()).getId();
        harness.addToBattlefield(player2, new SlingshotGoblin());

        castSparkcaster(player2.getId());
        resolveUntilPermanentChoice();

        UUID sparkcasterId = harness.getPermanentId(player1, "Sparkcaster");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(greenId, redId, sparkcasterId)
                .doesNotContain(blueId);
    }

    @Test
    @DisplayName("ETB returns the chosen red or green creature and deals 1 damage to the target player")
    void etbReturnsChosenCreatureAndDamagesPlayer() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new SlingshotGoblin()).getId();
        harness.setLife(player2, 20);

        castSparkcaster(player2.getId());
        resolveUntilPermanentChoice();
        harness.handlePermanentChosen(player1, goblinId);

        harness.assertInHand(player1, "Slingshot Goblin");
        harness.assertOnBattlefield(player1, "Sparkcaster");
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB damage can target Sparkcaster's controller")
    void etbDamagesItsController() {
        harness.setLife(player1, 20);

        castSparkcaster(player1.getId());
        resolveUntilPermanentChoice();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sparkcaster"));

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB damage can target a planeswalker")
    void etbDamagesPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        castSparkcaster(planeswalker.getId());
        resolveUntilPermanentChoice();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sparkcaster"));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertInHand(player1, "Sparkcaster");
    }

    @Test
    @DisplayName("ETB damage cannot target a creature")
    void etbCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlphaKavu());
        harness.enterBattlefieldAndReturn(player1, new Sparkcaster());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sparkcaster can be cast before choosing the damage trigger's target")
    void castingDoesNotRequireAnEtbTarget() {
        harness.castFromHand(player1, new Sparkcaster(), "{2}{R}{G}");

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Sparkcaster");
    }

    @Test
    @DisplayName("Entering creates two separate triggered abilities")
    void enteringCreatesSeparateTriggers() {
        castSparkcaster(player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sparkcaster");
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The controller can return Sparkcaster before its damage ability resolves")
    void canResolveReturnBeforeDamage() {
        castSparkcaster(player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2: Sparkcaster's ETB ability");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sparkcaster"));

        harness.assertInHand(player1, "Sparkcaster");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An illegal damage target does not stop the mandatory creature return")
    void illegalDamageTargetDoesNotStopReturn() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        castSparkcaster(planeswalker.getId());
        harness.passBothPriorities();
        chooseDamageFirstIfOrderingRequested();

        planeswalker.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player2, "Jace Beleren");
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) == null) {
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Sparkcaster"));
        harness.assertInHand(player1, "Sparkcaster");
        harness.assertNotOnBattlefield(player1, "Sparkcaster");
    }

    private void castSparkcaster(UUID targetId) {
        prepareSparkcaster();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void prepareSparkcaster() {
        harness.setHand(player1, List.of(new Sparkcaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void resolveUntilPermanentChoice() {
        harness.passBothPriorities();
        chooseDamageFirstIfOrderingRequested();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void chooseDamageFirstIfOrderingRequested() {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (choice != null && choice.context() instanceof ChoiceContext.SpellCastTriggerOrder) {
            harness.handleListChoice(player1, "1: Sparkcaster's ETB ability");
        }
    }
}
