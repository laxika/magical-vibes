package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.y.YoreTillerNephilim;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraugrRecruiter.class, DraugrsHelm.class, YoreTillerNephilim.class})
class DraugrRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("Boast returns a target creature card from the graveyard to hand")
    void boastReturnsCreatureToHand() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card creature = new DraugrRecruiter();
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
        Card creature = new DraugrRecruiter();
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
        Card firstCreature = new DraugrRecruiter();
        Card secondCreature = new DraugrRecruiter();
        recruiter.setAttackedThisTurn(true);
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(firstCreature.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boast cannot target a noncreature card")
    void boastCannotTargetNoncreature() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card noncreature = new DraugrsHelm();
        recruiter.setAttackedThisTurn(true);
        harness.setGraveyard(player1, List.of(noncreature));
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boast can be activated while tapped after actually attacking")
    void boastAfterDeclaredAttack() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card creature = new DraugrRecruiter();
        harness.setGraveyard(player1, List.of(creature));

        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        addBoastMana();
        assertThat(recruiter.isTapped()).isTrue();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(recruiter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering tapped and attacking does not qualify for boast")
    void enteringAttackingDoesNotAllowBoast() {
        Card recruiter = new DraugrRecruiter();
        Card creature = new DraugrRecruiter();
        harness.setGraveyard(player1, List.of(creature, recruiter));
        addCreatureReady(player1, new YoreTillerNephilim());

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(recruiter.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Draugr Recruiter");
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card creature = new DraugrRecruiter();
        recruiter.setAttackedThisTurn(true);
        harness.setGraveyard(player2, List.of(creature));
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Boast requires exactly one target creature card")
    void cannotChooseMultipleCreatures() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card first = new DraugrRecruiter();
        Card second = new DraugrRecruiter();
        recruiter.setAttackedThisTurn(true);
        harness.setGraveyard(player1, List.of(first, second));
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boast requires a target even when the graveyard is empty")
    void cannotActivateWithoutTarget() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        recruiter.setAttackedThisTurn(true);
        harness.setGraveyard(player1, List.of());
        addBoastMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boast does not return a target removed before resolution and still uses its activation")
    void removedTargetDoesNotReturnOrRefundActivation() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card first = new DraugrRecruiter();
        Card second = new DraugrRecruiter();
        recruiter.setAttackedThisTurn(true);
        harness.setGraveyard(player1, List.of(first, second));
        addBoastMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(first.getId()));
        harness.setGraveyard(player1, List.of(second));
        harness.setExile(player1, List.of(first));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
        addBoastMana();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boast resolves independently of its source")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent recruiter = addCreatureReady(player1, new DraugrRecruiter());
        Card creature = new DraugrRecruiter();
        recruiter.setAttackedThisTurn(true);
        harness.setGraveyard(player1, List.of(creature));
        addBoastMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, recruiter));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(recruiter.getCard());
    }

    private void addBoastMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
