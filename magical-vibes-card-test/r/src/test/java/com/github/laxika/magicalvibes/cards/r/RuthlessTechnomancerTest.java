package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessTechnomancer.class, GrizzlyBears.class, LeoninScimitar.class,
        Ornithopter.class, SerraAngel.class, Spellbook.class})
class RuthlessTechnomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another creature and creates Treasures equal to its power")
    void sacrificesAnotherCreatureAndCreatesTreasuresEqualToPower() {
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        fodder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castTechnomancer();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the sacrifice creates no Treasures")
    void decliningTheSacrificeCreatesNoTreasures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castTechnomancer();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices X artifacts and returns a creature with power X or less")
    void sacrificesArtifactsAndReturnsCreatureWithinPowerLimit() {
        Permanent technomancer = harness.addToBattlefieldAndReturn(player1, new RuthlessTechnomancer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, new SerraAngel()));
        addReanimationMana();

        harness.activateAbility(player1, indexOf(technomancer), 0, 2, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Rejects zero X and creatures whose power exceeds X")
    void rejectsZeroXAndTooPowerfulTargets() {
        Permanent technomancer = harness.addToBattlefieldAndReturn(player1, new RuthlessTechnomancer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        Card target = new SerraAngel();
        harness.setGraveyard(player1, List.of(target));
        addReanimationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(technomancer), 0, 2, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(technomancer), 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Spellbook")).hasSize(1);
        assertThat(findPermanents(player1, "Leonin Scimitar")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot sacrifice the source or an opponent's creature")
    void sacrificeChoiceExcludesSourceAndOpponentsCreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTechnomancer();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(
                player1, findPermanent(player1, "Ruthless Technomancer").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, fodder.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ruthless Technomancer");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Having no other creature creates no Treasures")
    void noOtherCreatureCreatesNoTreasures() {
        castTechnomancer();
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertOnBattlefield(player1, "Ruthless Technomancer");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("X may exceed the returned creature's power")
    void returnsCreatureWithPowerLessThanX() {
        Permanent technomancer = harness.addToBattlefieldAndReturn(player1, new RuthlessTechnomancer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        addReanimationMana();

        harness.activateAbility(player1, indexOf(technomancer), 0, 3, target.getId(), Zone.GRAVEYARD);
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects cards from an opponent's graveyard and noncreature cards")
    void rejectsOpponentsGraveyardAndNoncreatureTargets() {
        Permanent technomancer = harness.addToBattlefieldAndReturn(player1, new RuthlessTechnomancer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        Card opponentTarget = new GrizzlyBears();
        Card noncreature = new Spellbook();
        harness.setGraveyard(player2, List.of(opponentTarget));
        harness.setGraveyard(player1, List.of(noncreature));
        addReanimationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(technomancer), 0, 2, opponentTarget.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(technomancer), 0, 2, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Opponent's artifacts cannot pay the activation cost")
    void rejectsActivationWithoutEnoughOwnArtifacts() {
        Permanent technomancer = harness.addToBattlefieldAndReturn(player1, new RuthlessTechnomancer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        addReanimationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(technomancer), 0, 2, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing a zero-power creature creates no Treasures")
    void sacrificingZeroPowerCreatureCreatesNoTreasures() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        castTechnomancer();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Zero-power targets still require a positive X")
    void zeroPowerTargetStillRequiresPositiveX() {
        Permanent technomancer = harness.addToBattlefieldAndReturn(player1, new RuthlessTechnomancer());
        harness.addToBattlefield(player1, new Spellbook());
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));
        addReanimationMana();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(technomancer), 0, 0, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Spellbook");
        harness.activateAbility(player1, indexOf(technomancer), 0, 1, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    private void castTechnomancer() {
        harness.castFromHand(player1, new RuthlessTechnomancer(), "{3}{B}");
    }

    private void addReanimationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
