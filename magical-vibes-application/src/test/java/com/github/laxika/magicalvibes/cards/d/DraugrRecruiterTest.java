package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DraugrRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("Boast returns a target creature card from the graveyard to hand")
    void boastReturnsCreatureToHand() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card creature = new GrizzlyBears();
        recruiter.setAttackedThisTurn(true);
        harness.setGraveyard(player1, List.of(creature));
        addBoastMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Boast requires Draugr Recruiter to have attacked this turn")
    void boastRequiresThisCreatureToHaveAttacked() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
        assertThat(recruiter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        recruiter.setAttackedThisTurn(true);
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(firstCreature.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }

    @Test
    @DisplayName("Boast cannot target a noncreature card")
    void boastCannotTargetNoncreature() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card noncreature = new HolyDay();
        recruiter.setAttackedThisTurn(true);
        harness.setGraveyard(player1, List.of(noncreature));
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBoastMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
