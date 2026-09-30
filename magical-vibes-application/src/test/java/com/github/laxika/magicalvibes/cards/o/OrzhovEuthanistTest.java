package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DouseInGloom;
import com.github.laxika.magicalvibes.cards.m.MourningThrull;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrzhovEuthanist.class, DouseInGloom.class, MourningThrull.class, OrzhovBasilica.class})
class OrzhovEuthanistTest extends BaseCardTest {

    @Test
    void entersAndDestroysCreatureThatWasDealtDamageThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MourningThrull());
        gd.permanentsDealtDamageThisTurn.add(target.getId());

        castEuthanist(target.getId());

        harness.assertNotOnBattlefield(player2, "Mourning Thrull");
        harness.assertInGraveyard(player2, "Mourning Thrull");
    }

    @Test
    void cannotTargetCreatureThatWasNotDealtDamageThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MourningThrull());
        harness.setHand(player1, List.of(new OrzhovEuthanist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    void cannotTargetNonCreatureEvenIfItWasDealtDamageThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrzhovBasilica());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new OrzhovEuthanist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }

    @Test
    void hauntedCreatureDeathDestroysAnotherCreatureThatWasDealtDamageThisTurn() {
        Permanent etbTarget = harness.addToBattlefieldAndReturn(player2, new MourningThrull());
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new MourningThrull());
        Permanent hauntedDeathTarget = harness.addToBattlefieldAndReturn(player2, new MourningThrull());
        gd.permanentsDealtDamageThisTurn.add(etbTarget.getId());

        castEuthanist(etbTarget.getId());
        gd.permanentsDealtDamageThisTurn.add(hauntedDeathTarget.getId());

        UUID euthanistId = harness.getPermanentId(player1, "Orzhov Euthanist");
        destroyWithDouseInGloom(euthanistId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();

        destroyWithDouseInGloom(hauntedCreature.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hauntedDeathTarget.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mourning Thrull");
    }

    @Test
    void hauntedCreatureDeathHasNoTriggerTargetWhenNoOtherCreatureWasDealtDamageThisTurn() {
        Permanent etbTarget = harness.addToBattlefieldAndReturn(player2, new MourningThrull());
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new MourningThrull());
        gd.permanentsDealtDamageThisTurn.add(etbTarget.getId());

        castEuthanist(etbTarget.getId());

        UUID euthanistId = harness.getPermanentId(player1, "Orzhov Euthanist");
        destroyWithDouseInGloom(euthanistId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();

        destroyWithDouseInGloom(hauntedCreature.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Orzhov Euthanist");
    }

    private void castEuthanist(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new OrzhovEuthanist()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroyWithDouseInGloom(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DouseInGloom()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
