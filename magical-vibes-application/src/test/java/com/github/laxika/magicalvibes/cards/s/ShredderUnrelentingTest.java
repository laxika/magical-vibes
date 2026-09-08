package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShredderUnrelenting.class, GrizzlyBears.class})
class ShredderUnrelentingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another creature you control deathtouch")
    void etbGrantsDeathtouch() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castShredder(bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Attacking gives another creature you control deathtouch")
    void attackGrantsDeathtouch() {
        Permanent shredder = addCreatureReady(player1, new ShredderUnrelenting());
        shredder.setSummoningSick(false);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castShredder(bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bears.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.setHand(player1, List.of(new ShredderUnrelenting()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, opponentBears.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    @DisplayName("Sneak puts Shredder onto the battlefield tapped and attacking")
    void sneaksOntoTheBattlefield() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShredderUnrelenting()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.playerAutoStopSteps.computeIfAbsent(player1.getId(), ignored -> new java.util.HashSet<>())
                .add(TurnStep.COMBAT_DAMAGE);
        gd.playerAutoStopSteps.computeIfAbsent(player2.getId(), ignored -> new java.util.HashSet<>())
                .add(TurnStep.COMBAT_DAMAGE);

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        Permanent shredder = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getClass() == ShredderUnrelenting.class)
                .findFirst().orElseThrow();
        assertThat(shredder.isTapped()).isTrue();
        assertThat(shredder.isAttacking()).isTrue();
        assertThat(shredder.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(ally.getId());
    }

    private void castShredder(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ShredderUnrelenting()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
