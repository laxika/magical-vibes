package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nucklavee.class, LavaAxe.class, Opt.class, Ponder.class, Shock.class})
class NucklaveeTest extends BaseCardTest {

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Nucklavee()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    void resolvingPutsOnBattlefield() {
        castAndResolve();
        harness.assertOnBattlefield(player1, "Nucklavee");
    }

    @Test
    void returnsRedSorceryAndBlueInstant() {
        LavaAxe axe = new LavaAxe();
        Opt opt = new Opt();
        harness.setGraveyard(player1, List.of(axe, opt));
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(axe.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(opt.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Opt");
        harness.assertInHand(player1, "Lava Axe");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningLeavesCardsInGraveyard() {
        LavaAxe axe = new LavaAxe();
        Opt opt = new Opt();
        harness.setGraveyard(player1, List.of(axe, opt));
        castAndResolve();

        harness.handleMultipleCardsChosen(player1, List.of(axe.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(opt.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Lava Axe");
        harness.assertInGraveyard(player1, "Opt");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void redTriggerRejectsRedInstant() {
        LavaAxe axe = new LavaAxe();
        harness.setGraveyard(player1, List.of(new Shock(), axe));
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(axe.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lava Axe");
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void blueTriggerRejectsBlueSorcery() {
        harness.setGraveyard(player1, List.of(new Ponder()));
        castAndResolve();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Ponder");
    }

    @Test
    void blueInstantCanBeReturnedWithoutRedSorcery() {
        Opt opt = new Opt();
        harness.setGraveyard(player1, List.of(opt));
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(opt.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Opt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentGraveyardCannotSupplyTargets() {
        harness.setGraveyard(player2, List.of(new LavaAxe(), new Opt()));
        castAndResolve();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Lava Axe");
        harness.assertInGraveyard(player2, "Opt");
    }

    @Test
    void mayChoicesAreIndependent() {
        LavaAxe axe = new LavaAxe();
        Opt opt = new Opt();
        harness.setGraveyard(player1, List.of(axe, opt));
        castAndResolve();

        harness.handleMultipleCardsChosen(player1, List.of(axe.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(opt.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Opt");
        harness.assertNotInHand(player1, "Opt");
        harness.assertInHand(player1, "Lava Axe");
    }

    @Test
    void removedTargetCannotBeReplacedWithAnotherCard() {
        Opt target = new Opt();
        Opt other = new Opt();
        harness.setGraveyard(player1, List.of(target, other));
        castAndResolve();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    void noLegalTargetsMeansNoAbilitiesOnStack() {
        castAndResolve();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
