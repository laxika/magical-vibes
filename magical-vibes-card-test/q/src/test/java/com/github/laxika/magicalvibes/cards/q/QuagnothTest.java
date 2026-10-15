package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AugurOfSkulls;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.s.SlaughterPact;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Quagnoth.class, AugurOfSkulls.class, SlaughterPact.class, NessianCourser.class})
class QuagnothTest extends BaseCardTest {

    @Test
    void splitSecondPreventsSpellsAndNonManaAbilitiesWhileCreatureSpellIsOnStack() {
        harness.addToBattlefield(player2, new AugurOfSkulls());
        harness.addToBattlefield(player2, new NessianCourser());
        harness.setHand(player2, List.of(new SlaughterPact()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new Quagnoth(), "{5}{G}");

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getPermanentId(player2, "Nessian Courser")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Quagnoth");
    }

    @Test
    void shroudPreventsBothPlayersFromTargetingQuagnoth() {
        harness.addToBattlefield(player1, new Quagnoth());
        harness.setHand(player1, List.of(new SlaughterPact()));
        harness.setHand(player2, List.of(new SlaughterPact()));
        var targetId = harness.getPermanentId(player1, "Quagnoth");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Quagnoth");
    }

    @Test
    void bothDiscardedQuagnothsReturnToHand() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player2, List.of(new Quagnoth(), new Quagnoth()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertNotInGraveyard(player2, "Quagnoth");
    }

    @Test
    void discardReturnAbilityDoesNotPreventRespondingWithASpell() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.addToBattlefield(player2, new NessianCourser());
        harness.setHand(player1, List.of(new SlaughterPact()));
        harness.setHand(player2, List.of(new Quagnoth(), new AugurOfSkulls()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player2, "Quagnoth");

        assertThatCode(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Nessian Courser")))
                .doesNotThrowAnyException();
        resolveAllTriggers();
        harness.assertInHand(player2, "Quagnoth");
        harness.assertInGraveyard(player2, "Nessian Courser");
    }

    @Test
    void discardReturnAbilityDoesNotPreventRespondingWithAnActivatedAbility() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.addToBattlefield(player2, new AugurOfSkulls());
        harness.setHand(player2, List.of(new Quagnoth(), new AugurOfSkulls()));
        advanceToUpkeep(player1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player2, "Quagnoth");

        assertThatCode(() -> harness.activateAbility(player2, 0, 0, null, null))
                .doesNotThrowAnyException();
        resolveAllTriggers();
        harness.assertInHand(player2, "Quagnoth");
    }

    @Test
    void returnsToHandWhenDiscardedByOpponent() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player2, List.of(new Quagnoth(), new AugurOfSkulls()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.passBothPriorities();

        harness.assertInHand(player2, "Quagnoth");
        harness.assertNotInGraveyard(player2, "Quagnoth");
    }

    @Test
    void doesNotTriggerWhenControllerDiscardsIt() {
        harness.addToBattlefield(player1, new AugurOfSkulls());
        harness.setHand(player1, List.of(new Quagnoth(), new AugurOfSkulls()));
        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Quagnoth");
        harness.assertNotInHand(player1, "Quagnoth");
    }
}
