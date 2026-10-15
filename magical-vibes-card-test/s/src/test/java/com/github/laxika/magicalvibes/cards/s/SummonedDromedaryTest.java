package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SummonedDromedary.class})
class SummonedDromedaryTest extends BaseCardTest {

    @Test
    void returnsFromGraveyardToHandAtSorcerySpeed() {
        SummonedDromedary dromedary = new SummonedDromedary();
        harness.setGraveyard(player1, List.of(dromedary));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dromedary.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(dromedary.getId()));
    }

    @Test
    void cannotActivateOutsideSorcerySpeed() {
        SummonedDromedary dromedary = new SummonedDromedary();
        harness.setGraveyard(player1, List.of(dromedary));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void returnsOnlyTheActivatedCopyDuringPostcombatMain() {
        SummonedDromedary source = new SummonedDromedary();
        SummonedDromedary other = new SummonedDromedary();
        SummonedDromedary opponentsCopy = new SummonedDromedary();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(other, source));
        harness.setGraveyard(player2, List.of(opponentsCopy));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, source);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCopy);
    }

    @Test
    void cannotActivateDuringUpkeep() {
        harness.setGraveyard(player1, List.of(new SummonedDromedary()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void cannotActivateWithAnAbilityAlreadyOnTheStack() {
        harness.setGraveyard(player1, List.of(new SummonedDromedary(), new SummonedDromedary()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();
    }

    @Test
    void cannotPayTheActivationCostWithoutWhiteMana() {
        SummonedDromedary source = new SummonedDromedary();
        harness.setGraveyard(player1, List.of(source));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReturnANewGraveyardIncarnationOfTheSource() {
        SummonedDromedary source = new SummonedDromedary();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(source));
        gd.markGraveyardEntry(source);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateGraveyardAbility(player1, 0);

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(source));
        gd.removeFromExile(source.getId());
        harness.setGraveyard(player1, List.of(source));
        gd.markGraveyardEntry(source);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void attackingDoesNotTapTheDromedary() {
        var permanent = addCreatureReady(player1, new SummonedDromedary());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(permanent.isTapped()).isFalse();
        assertThat(permanent.isAttacking()).isTrue();
    }
}
