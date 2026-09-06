package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Insurrection.class, GrizzlyBears.class, Mountain.class})
class InsurrectionTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all creatures, gains control of them, and grants haste")
    void untapsStealsAndGrantsHaste() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        opponentCreature.tap();
        ownCreature.tap();
        opponentLand.tap();

        castInsurrection();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentLand);
        assertThat(opponentLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Temporary control and haste expire at end of turn")
    void controlAndHasteExpireAtEndOfTurn() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castInsurrection();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opponentCreature);
        assertThat(opponentCreature.hasKeyword(Keyword.HASTE)).isFalse();
    }

    private void castInsurrection() {
        harness.setHand(player1, List.of(new Insurrection()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
