package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PlagueDrone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreyKnightParagon.class, PlagueDrone.class})
class GreyKnightParagonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and destroys a non-Demon attacking creature")
    void destroysNonDemonAttacker() {
        Permanent attacker = addAttacker(player2, new GreyKnightParagon());

        castGreyKnightParagon(attacker);
        resolveEntryTrigger();

        harness.assertNotOnBattlefield(player2, "Grey Knight Paragon");
        harness.assertInGraveyard(player2, "Grey Knight Paragon");
        harness.assertOnBattlefield(player1, "Grey Knight Paragon");
    }

    @Test
    @DisplayName("Enters and exiles an attacking Demon")
    void exilesDemonAttacker() {
        Permanent attacker = addAttacker(player2, new PlagueDrone());

        castGreyKnightParagon(attacker);
        resolveEntryTrigger();

        harness.assertNotOnBattlefield(player2, "Plague Drone");
        harness.assertNotInGraveyard(player2, "Plague Drone");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Plague Drone");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreyKnightParagon());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GreyKnightParagon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    @DisplayName("A creature that stops attacking before resolution is not destroyed")
    void doesNotDestroyFormerAttacker() {
        Permanent attacker = addAttacker(player2, new GreyKnightParagon());
        castGreyKnightParagon(attacker);
        harness.passBothPriorities();

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grey Knight Paragon");
        harness.assertNotInGraveyard(player2, "Grey Knight Paragon");
        harness.assertOnBattlefield(player1, "Grey Knight Paragon");
    }

    @Test
    @DisplayName("A Demon that stops attacking before resolution is not exiled")
    void doesNotExileFormerDemonAttacker() {
        Permanent attacker = addAttacker(player2, new PlagueDrone());
        castGreyKnightParagon(attacker);
        harness.passBothPriorities();

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Plague Drone");
        harness.assertNotInGraveyard(player2, "Plague Drone");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Non-Demon destruction permits regeneration")
    void nonDemonCanRegenerate() {
        Permanent attacker = addAttacker(player2, new GreyKnightParagon());
        attacker.setRegenerationShield(1);

        castGreyKnightParagon(attacker);
        resolveEntryTrigger();

        harness.assertOnBattlefield(player2, "Grey Knight Paragon");
        harness.assertNotInGraveyard(player2, "Grey Knight Paragon");
        assertThat(attacker.getRegenerationShield()).isZero();
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiling a Demon bypasses regeneration")
    void demonCannotRegenerateFromExile() {
        Permanent attacker = addAttacker(player2, new PlagueDrone());
        attacker.setRegenerationShield(1);

        castGreyKnightParagon(attacker);
        resolveEntryTrigger();

        harness.assertNotOnBattlefield(player2, "Plague Drone");
        harness.assertNotInGraveyard(player2, "Plague Drone");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Plague Drone");
    }

    @Test
    @DisplayName("The entry trigger resolves after Grey Knight Paragon leaves")
    void triggerResolvesWithoutSource() {
        Permanent attacker = addAttacker(player2, new PlagueDrone());
        castGreyKnightParagon(attacker);
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, source));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grey Knight Paragon");
        harness.assertNotOnBattlefield(player2, "Plague Drone");
        harness.assertNotInGraveyard(player2, "Plague Drone");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Plague Drone");
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player, card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private void castGreyKnightParagon(Permanent target) {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GreyKnightParagon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, target.getId());
    }

    private void resolveEntryTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
