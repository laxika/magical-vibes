package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicineBagTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Medicine Bag starts a discard-cost choice")
    void activationStartsDiscardChoice() {
        addReadyMedicineBag();
        Permanent target = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discarding a card grants a regeneration shield to the target creature")
    void discardGrantsRegenerationShield() {
        addReadyMedicineBag();
        Permanent target = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Medicine Bag cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyMedicineBag();
        Permanent artifact = addNoncreatureArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Medicine Bag cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadyMedicineBag();
        Permanent target = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMedicineBag() {
        Permanent bag = new Permanent(new MedicineBag());
        bag.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bag);
        return bag;
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private Permanent addNoncreatureArtifact(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new Spellbook());
    }
}
