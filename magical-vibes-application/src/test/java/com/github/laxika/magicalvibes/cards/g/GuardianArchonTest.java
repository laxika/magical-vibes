package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianArchon.class, Shock.class, ActOfTreason.class})
class GuardianArchonTest extends BaseCardTest {

    @Test
    void secretlyChoosesOpponentAndProtectsControllerAndTargetPermanent() {
        Permanent archon = castGuardianArchon();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, archon.getId());
        harness.passBothPriorities();

        assertThat(archon.getProtectionFromPlayerIdsUntilEndOfTurn()).contains(player2.getId());
        assertThat(gd.playerProtectionFromPlayerIdsUntilEndOfTurn.get(player1.getId()))
                .contains(player2.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, archon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void requiresOpponentAndCanActivateOnlyOnce() {
        Permanent archon = castGuardianArchon();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, archon.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, archon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void newControllerCannotRevealAnotherPlayersSecretChoice() {
        Permanent archon = castGuardianArchon();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player2, 0, 0, archon.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(archon);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, archon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotHaveProtectionBeforeActivating() {
        Permanent archon = castGuardianArchon();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, archon.getId());

        assertThat(archon.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void illegalTargetPreventsProtectionForControllerToo() {
        Permanent archon = castGuardianArchon();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GuardianArchon());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getProtectionFromPlayerIdsUntilEndOfTurn()).isEmpty();
        assertThat(gd.playerProtectionFromPlayerIdsUntilEndOfTurn.getOrDefault(player1.getId(), java.util.Set.of()))
                .isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, archon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    void abilityStillResolvesAfterItsSourceDies() {
        Permanent archon = castGuardianArchon();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GuardianArchon());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, archon.getId());
        harness.castAndResolveInstant(player2, 0, archon.getId());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, archon.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(archon);
        assertThat(target.getProtectionFromPlayerIdsUntilEndOfTurn()).contains(player2.getId());
        assertThat(gd.playerProtectionFromPlayerIdsUntilEndOfTurn.get(player1.getId()))
                .contains(player2.getId());
    }

    @Test
    void cannotActivateWithoutAnAsEntersChoice() {
        Permanent archon = harness.addToBattlefieldAndReturn(player1, new GuardianArchon());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, archon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No player was chosen");
    }

    @Test
    void protectionExpiresAtEndOfTurnButActivationLimitDoesNot() {
        Permanent archon = castGuardianArchon();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.activateAbility(player1, 0, 0, null, archon.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(archon.getProtectionFromPlayerIdsUntilEndOfTurn()).isEmpty();
        assertThat(gd.playerProtectionFromPlayerIdsUntilEndOfTurn.getOrDefault(player1.getId(), java.util.Set.of()))
                .isEmpty();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, archon.getId());
        assertThat(archon.getMarkedDamage()).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, archon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent castGuardianArchon() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GuardianArchon(), "{4}{W}{W}");
        harness.passBothPriorities();
        return findPermanent(player1, "Guardian Archon");
    }
}
