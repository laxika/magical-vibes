package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CleaverRiotTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants double strike to all creatures you control")
    void grantsDoubleStrikeToOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);
        for (Permanent p : battlefield) {
            assertThat(p.getGrantedKeywords()).contains(Keyword.DOUBLE_STRIKE);
        }
    }

    @Test
    @DisplayName("Does not grant double strike to opponent's creatures")
    void doesNotGrantToOpponentCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> p2Battlefield = gd.playerBattlefields.get(player2.getId());
        for (Permanent p : p2Battlefield) {
            assertThat(p.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
        }
    }

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        for (Permanent p : battlefield) {
            assertThat(p.getGrantedKeywords()).doesNotContain(Keyword.DOUBLE_STRIKE);
        }
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new CleaverRiot()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cleaver Riot");
    }
}
