package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Ashnod's Intervention")
class AshnodsInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+0 until end of turn")
    void boostsTargetCreature() {
        Permanent creature = addCreature();

        castOn(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature returns to its owner's hand when it dies")
    void returnsToHandOnDeath() {
        Permanent creature = addCreature();
        Card creatureCard = creature.getCard();

        castOn(creature);
        destroy(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(creatureCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).doesNotContain(creatureCard.getId());
    }

    @Test
    @DisplayName("Creature returns to its owner's hand when it is exiled")
    void returnsToHandOnExile() {
        Permanent creature = addCreature();
        Card creatureCard = creature.getCard();

        castOn(creature);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(creatureCard.getId());
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The granted abilities expire at end of turn")
    void grantedAbilitiesExpireAtEndOfTurn() {
        Permanent creature = addCreature();
        Card creatureCard = creature.getCard();

        castOn(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroy(creature);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(creatureCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).doesNotContain(creatureCard.getId());
    }

    private Permanent addCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        return gd.playerBattlefields.get(player1.getId()).getLast();
    }

    private void castOn(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AshnodsIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    private void destroy(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
    }
}
