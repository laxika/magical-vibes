package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.model.Keyword;
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

@DisplayName("Defiler of Flesh")
@CardUsed({DefilerOfFlesh.class, GrizzlyBears.class, TyphoidRats.class})
class DefilerOfFleshTest extends BaseCardTest {

    @Test
    @DisplayName("paying 2 life reduces a black permanent spell by {B} and boosts a controlled creature")
    void paysLifeForBlackPermanentSpell() {
        addCreatureReady(player1, new DefilerOfFlesh());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TyphoidRats()));
        harness.setLife(player1, 20);

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, null, false, null, null, null, List.of(), List.of(), false,
                null, null, List.of(), List.of(), null, null, false, true, null);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("paying the reduced black mana cost leaves life unchanged")
    void paysReducedManaForBlackPermanentSpell() {
        addCreatureReady(player1, new DefilerOfFlesh());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TyphoidRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("the trigger targets only creatures controlled by its controller")
    void targetIsRestrictedToControlledCreatures() {
        addCreatureReady(player1, new DefilerOfFlesh());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TyphoidRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opposingCreature.getId());
    }

    @Test
    @DisplayName("the boost and menace wear off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new DefilerOfFlesh());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TyphoidRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.MENACE)).isFalse();
    }
}
