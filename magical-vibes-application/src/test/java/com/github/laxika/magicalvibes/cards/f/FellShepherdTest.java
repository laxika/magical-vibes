package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FellShepherd.class, GrizzlyBears.class, AirElemental.class, Shock.class, Forest.class})
class FellShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may return this turn's battlefield creature cards to hand")
    void combatDamageMayReturnThisTurnsCreatures() {
        Card alreadyInGraveyard = new GrizzlyBears();
        Card diedThisTurn = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(alreadyInGraveyard));

        addCreatureReady(player1, new FellShepherd());
        Permanent deadCreature = addCreatureReady(player1, diedThisTurn);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, deadCreature.getId());
        harness.passBothPriorities();

        Permanent shepherd = gd.playerBattlefields.get(player1.getId()).getFirst();
        shepherd.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(diedThisTurn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(alreadyInGraveyard)
                .noneMatch(card -> card.getId().equals(diedThisTurn.getId()));
    }

    @Test
    @DisplayName("Declining the combat-damage may-trigger returns nothing")
    void decliningCombatDamageTriggerReturnsNothing() {
        Card diedThisTurn = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(diedThisTurn));
        addCreatureReady(player1, new FellShepherd()).setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(diedThisTurn.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(diedThisTurn);
    }

    @Test
    @DisplayName("Sacrificing another creature gives the target -2/-2 until end of turn")
    void sacrificesAnotherCreatureAndShrinksTarget() {
        Permanent shepherd = addCreatureReady(player1, new FellShepherd());
        Card fodderCard = new GrizzlyBears();
        addCreatureReady(player1, fodderCard);
        Permanent target = addCreatureReady(player2, new AirElemental());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shepherd);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fodderCard);
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new FellShepherd());
        Card fodderCard = new GrizzlyBears();
        addCreatureReady(player1, fodderCard);
        Permanent forest = addCreatureReady(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(fodderCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(fodderCard);
    }
}
