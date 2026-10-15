package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.b.BreedingPool;
import com.github.laxika.magicalvibes.cards.o.OcularHalo;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SimicGuildmage.class, AssaultZeppelid.class, OcularHalo.class, BreedingPool.class})
class SimicGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Moves a +1/+1 counter between creatures with the same controller")
    void movesPlusOneCounter() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent source = addCreatureReady(player1, new AssaultZeppelid());
        Permanent destination = addCreatureReady(player1, new AssaultZeppelid());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        activateCounterAbility(source, destination);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter ability moves only a +1/+1 counter")
    void movesOnlyPlusOnePlusOneCounter() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent source = addCreatureReady(player1, new AssaultZeppelid());
        Permanent destination = addCreatureReady(player1, new AssaultZeppelid());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        source.setCounterCount(CounterType.CHARGE, 2);

        activateCounterAbility(source, destination);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(destination.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The counter ability requires another creature as its second target")
    void counterAbilityRejectsSameCreatureForBothTargets() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareAbilityActivation(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The counter ability rejects creatures controlled by different players")
    void counterAbilityRejectsDifferentControllers() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent source = addCreatureReady(player1, new AssaultZeppelid());
        Permanent opponentCreature = addCreatureReady(player2, new AssaultZeppelid());
        prepareAbilityActivation(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(source.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("controlled by");
    }

    @Test
    @DisplayName("The counter ability does nothing when the controllers differ at resolution")
    void counterAbilityChecksControllersAtResolution() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent source = addCreatureReady(player1, new AssaultZeppelid());
        Permanent destination = addCreatureReady(player1, new AssaultZeppelid());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareAbilityActivation(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), destination.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(destination);
        gd.playerBattlefields.get(player2.getId()).add(destination);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Moves a target Aura to a permanent controlled by its host's controller")
    void movesAuraToSameControllerPermanent() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent host = addCreatureReady(player2, new AssaultZeppelid());
        Permanent recipient = addCreatureReady(player2, new AssaultZeppelid());
        Permanent secondRecipient = addCreatureReady(player2, new AssaultZeppelid());
        Permanent invalidRecipient = harness.addToBattlefieldAndReturn(player2, new BreedingPool());
        Permanent aura = addAura(player1, new OcularHalo(), host);

        prepareAbilityActivation(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, aura.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(recipient.getId(), secondRecipient.getId())
                .doesNotContain(host.getId(), invalidRecipient.getId(),
                        gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttachTargetAuraToAnotherPermanentWithSameController.class);

        harness.handlePermanentChosen(player1, recipient.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(recipient.getId());
    }

    @Test
    @DisplayName("The Aura ability rejects a target that is not an attached Aura")
    void auraAbilityRejectsNonAuraTarget() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent creature = addCreatureReady(player1, new AssaultZeppelid());
        prepareAbilityActivation(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a permanent");
    }

    @Test
    @DisplayName("A source without a +1/+1 counter is legal and moves nothing")
    void counterAbilityWithoutCounterDoesNothing() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent source = addCreatureReady(player2, new AssaultZeppelid());
        Permanent destination = addCreatureReady(player2, new AssaultZeppelid());
        source.setCounterCount(CounterType.CHARGE, 1);

        activateCounterAbility(source, destination);

        assertThat(source.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Both counter targets may change controller together before resolution")
    void counterAbilityAllowsBothControllersToChange() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent source = addCreatureReady(player1, new AssaultZeppelid());
        Permanent destination = addCreatureReady(player1, new AssaultZeppelid());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareAbilityActivation(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), destination.getId()));

        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(source, destination));
        gd.playerBattlefields.get(player2.getId()).addAll(List.of(source, destination));
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An Aura stays attached when there is no legal destination")
    void auraAbilityWithNoLegalDestinationDoesNothing() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent host = addCreatureReady(player2, new AssaultZeppelid());
        harness.addToBattlefield(player2, new BreedingPool());
        Permanent aura = addAura(player1, new OcularHalo(), host);
        prepareAbilityActivation(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, aura.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An Aura moves without a prompt when only one legal destination exists")
    void auraAbilityWithOneDestinationMovesWithoutPrompt() {
        addCreatureReady(player1, new SimicGuildmage());
        Permanent host = addCreatureReady(player2, new AssaultZeppelid());
        Permanent destination = addCreatureReady(player2, new AssaultZeppelid());
        Permanent aura = addAura(player1, new OcularHalo(), host);
        prepareAbilityActivation(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, aura.getId());
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addAura(Player player, com.github.laxika.magicalvibes.model.Card auraCard, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, auraCard);
        aura.setAttachedTo(host.getId());
        return aura;
    }

    private void activateCounterAbility(Permanent source, Permanent destination) {
        prepareAbilityActivation(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(source.getId(), destination.getId()));
        harness.passBothPriorities();
    }

    private void prepareAbilityActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
