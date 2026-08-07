package com.github.laxika.magicalvibes.cards.b;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoneDancerTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new BoneDancer());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private void attackUnblocked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        // Advance into the declare-blockers step (the defender has no blockers), firing the
        // "attacks and isn't blocked" trigger, then resolve it to present the may choice.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting reanimates the top creature card of the defending player's graveyard under your control")
    void unblockedAcceptReanimatesTopCreatureCardOfDefenderGraveyard() {
        Permanent attacker = addAttacker();
        Card bears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        harness.setGraveyard(player2, List.of(bears, hillGiant));

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(hillGiant.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(bears.getId())
                .doesNotContain(hillGiant.getId());

        // "If you do, this creature assigns no combat damage this turn."
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Noncreature cards above the top creature card are skipped")
    void skipsNoncreatureCardsAboveTheTopCreatureCard() {
        addAttacker();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player2, List.of(bears, shock));

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(bears.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(shock.getId())
                .doesNotContain(bears.getId());
    }

    @Test
    @DisplayName("Nothing is reanimated from an empty graveyard, so the Dancer still deals its combat damage")
    void emptyGraveyardStillDealsCombatDamage() {
        Permanent attacker = addAttacker();
        harness.setGraveyard(player2, List.of());

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Declining leaves the graveyard alone and the Dancer deals its combat damage")
    void unblockedDeclineLeavesGraveyardAlone() {
        Permanent attacker = addAttacker();
        Card hillGiant = new HillGiant();
        harness.setGraveyard(player2, List.of(hillGiant));

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(hillGiant.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        addAttacker();
        Card hillGiant = new HillGiant();
        harness.setGraveyard(player2, List.of(hillGiant));

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(hillGiant.getId());
    }
}
