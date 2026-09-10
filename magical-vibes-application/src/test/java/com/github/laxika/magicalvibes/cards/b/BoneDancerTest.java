package com.github.laxika.magicalvibes.cards.b;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.a.Abeyance;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({BoneDancer.class, BenalishKnight.class, Abeyance.class})
class BoneDancerTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new BoneDancer());
        attacker.setAttacking(true);
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
        Card lowerCreature = new BenalishKnight();
        Card topCreature = new BenalishKnight();
        harness.setGraveyard(player2, List.of(lowerCreature, topCreature));

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(topCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(lowerCreature.getId())
                .doesNotContain(topCreature.getId());

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
        Card creature = new BenalishKnight();
        Card noncreature = new Abeyance();
        harness.setGraveyard(player2, List.of(creature, noncreature));

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(noncreature.getId())
                .doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("Accepting with no creature card leaves the graveyard alone and deals combat damage")
    void noCreatureCardStillDealsCombatDamage() {
        Permanent attacker = addAttacker();
        Card noncreature = new Abeyance();
        harness.setGraveyard(player2, List.of(noncreature));

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .doesNotContain(noncreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(noncreature.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
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
        Card creature = new BenalishKnight();
        harness.setGraveyard(player2, List.of(creature));

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        addAttacker();
        Card creature = new BenalishKnight();
        harness.setGraveyard(player2, List.of(creature));

        addCreatureReady(player2, new BenalishKnight());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
    }
}
