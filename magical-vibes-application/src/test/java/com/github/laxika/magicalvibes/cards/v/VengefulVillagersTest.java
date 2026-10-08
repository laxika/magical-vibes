package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VengefulVillagers.class, GrizzlyBears.class, Spellbook.class})
class VengefulVillagersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps an opponent creature and may stun it by sacrificing an artifact")
    void attackingSacrificesArtifactForStunCounter() {
        addCreatureReady(player1, new VengefulVillagers());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Declining the sacrifice still taps the chosen creature")
    void decliningSacrificeDoesNotAddStunCounter() {
        addCreatureReady(player1, new VengefulVillagers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Own creatures are not legal attack-trigger targets")
    void ownCreatureCannotBeTargeted() {
        addCreatureReady(player1, new VengefulVillagers());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attacking Villagers may sacrifice themselves to stun the chosen creature")
    void attackerCanSacrificeItself() {
        Permanent attacker = addCreatureReady(player1, new VengefulVillagers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VengefulVillagers());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        harness.assertInGraveyard(player1, "Vengeful Villagers");
        harness.assertNotOnBattlefield(player1, "Vengeful Villagers");
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("An already tapped creature can be stunned and skips its next untap")
    void alreadyTappedCreatureCanBeStunned() {
        addCreatureReady(player1, new VengefulVillagers());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new VengefulVillagers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VengefulVillagers());
        target.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With no opposing creature there is no sacrifice choice")
    void noLegalTargetDoesNotOfferSacrifice() {
        addCreatureReady(player1, new VengefulVillagers());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Vengeful Villagers");
        harness.assertNotInGraveyard(player1, "Vengeful Villagers");
    }
}
