package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.cards.n.NeurokProdigy;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrakensEye.class, NeurokProdigy.class, CrazedGoblin.class, EchoingTruth.class})
class KrakensEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Kraken's Eye puts it on the stack as an artifact spell")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new KrakensEye(), "{2}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(KrakensEye.class);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Kraken's Eye resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new KrakensEye(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof KrakensEye);
    }

    @Test
    @DisplayName("Controller casts blue spell, accepts may ability, gains 1 life")
    void controllerCastsBlueSpellAndAccepts() {
        harness.addToBattlefield(player1, new KrakensEye());

        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new NeurokProdigy(), "{2}{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard() instanceof KrakensEye);
        harness.passBothPriorities();

        // Player1 should be prompted for may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts blue spell, declines may ability, no life gain")
    void controllerCastsBlueSpellAndDeclines() {
        harness.addToBattlefield(player1, new KrakensEye());

        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new NeurokProdigy(), "{2}{U}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard() instanceof KrakensEye);

        // Resolve the creature spell
        harness.passBothPriorities();

        // No life gained
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Opponent casts blue spell, controller accepts may ability, gains 1 life")
    void opponentCastsBlueSpellControllerAccepts() {
        harness.addToBattlefield(player1, new KrakensEye());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player2, new NeurokProdigy(), "{2}{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();

        // Player1 (controller of Kraken's Eye) should be prompted
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability and then the creature spell
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Casting a blue instant also triggers Kraken's Eye")
    void blueInstantSpellTriggers() {
        harness.addToBattlefield(player1, new KrakensEye());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CrazedGoblin()).getId();
        harness.setHand(player1, List.of(new EchoingTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, targetId);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card instanceof CrazedGoblin);
    }

    @Test
    @DisplayName("Triggered life gain resolves after Kraken's Eye leaves the battlefield")
    void triggeredAbilityResolvesAfterSourceLeavesBattlefield() {
        UUID eyeId = harness.addToBattlefieldAndReturn(player1, new KrakensEye()).getId();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new NeurokProdigy(), "{2}{U}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.setHand(player1, List.of(new EchoingTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, eyeId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof KrakensEye);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof KrakensEye);
    }

    @Test
    @DisplayName("Non-blue spell does not trigger Kraken's Eye")
    void nonBlueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KrakensEye());

        harness.castFromHand(player1, new CrazedGoblin(), "{R}");

        // Should not be awaiting may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Multiple Kraken's Eyes each trigger independently")
    void multipleEyesTriggerIndependently() {
        harness.addToBattlefield(player1, new KrakensEye());
        harness.addToBattlefield(player1, new KrakensEye());

        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new NeurokProdigy(), "{2}{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY).count()).isEqualTo(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Kraken's Eye does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        // Kraken's Eye is not on the battlefield
        harness.castFromHand(player1, new NeurokProdigy(), "{2}{U}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
    @Test
    @DisplayName("Only the Eye's controller gains life when the other player casts blue")
    void opponentControlledEyeGainsLifeForItsController() {
        harness.addToBattlefield(player2, new KrakensEye());
        int casterLife = gd.getLife(player1.getId());
        int controllerLife = gd.getLife(player2.getId());

        harness.castFromHand(player1, new NeurokProdigy(), "{2}{U}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        harness.assertLife(player1, casterLife);
        harness.assertLife(player2, controllerLife + 1);
    }

    @Test
    @DisplayName("Casting a colorless artifact does not trigger an existing Eye")
    void colorlessSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KrakensEye());
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new KrakensEye(), "{2}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof KrakensEye).hasSize(2);
    }

}
