package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({SimicGuildmage.class, GrizzlyBears.class, Pacifism.class})
class SimicGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Moves a +1/+1 counter between creatures with the same controller")
    void movesPlusOneCounter() {
        addReadyGuildmage(player1);
        Permanent source = addCreature(player1);
        Permanent destination = addCreature(player1);
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        activateCounterAbility(source, destination);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(destination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter ability rejects creatures controlled by different players")
    void counterAbilityRejectsDifferentControllers() {
        addReadyGuildmage(player1);
        Permanent source = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);
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
        addReadyGuildmage(player1);
        Permanent source = addCreature(player1);
        Permanent destination = addCreature(player1);
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
        addReadyGuildmage(player1);
        Permanent host = addCreature(player2);
        Permanent recipient = addCreature(player2);
        Permanent secondRecipient = addCreature(player2);
        Permanent aura = addAura(player1, new Pacifism(), host);

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
                .doesNotContain(host.getId(), gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttachTargetAuraToAnotherPermanentWithSameController.class);

        harness.handlePermanentChosen(player1, recipient.getId());

        assertThat(aura.getAttachedTo()).isEqualTo(recipient.getId());
    }

    @Test
    @DisplayName("The Aura ability rejects a target that is not an attached Aura")
    void auraAbilityRejectsNonAuraTarget() {
        addReadyGuildmage(player1);
        Permanent creature = addCreature(player1);
        prepareAbilityActivation(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura attached to a permanent");
    }

    private Permanent addReadyGuildmage(Player player) {
        Permanent guildmage = new Permanent(new SimicGuildmage());
        guildmage.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(guildmage);
        return guildmage;
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addAura(Player player, com.github.laxika.magicalvibes.model.Card auraCard, Permanent host) {
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(player.getId()).add(aura);
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
