package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExpeditionSupplier.class, BeskirShieldmate.class, GrizzlyBears.class})
class ExpeditionSupplierTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures and attaches a Utility Knife when a Human or Warrior enters")
    void conjuresUtilityKnifeForHumanOrWarrior() {
        addCreatureReady(player1, new ExpeditionSupplier());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castShieldmate();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent knife = findPermanent(player1, "Utility Knife");
        assertThat(knife.getCard().isToken()).isTrue();
        assertThat(knife.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        addCreatureReady(player1, new ExpeditionSupplier());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BeskirShieldmate(), new BeskirShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        chooseKnifeTarget();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Utility Knife")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Human, non-Warrior creature")
    void ignoresOtherCreatures() {
        addCreatureReady(player1, new ExpeditionSupplier());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Utility Knife")).isZero();
    }

    private void castShieldmate() {
        harness.setHand(player1, List.of(new BeskirShieldmate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void chooseKnifeTarget() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent target = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, target.getId());
    }
}
