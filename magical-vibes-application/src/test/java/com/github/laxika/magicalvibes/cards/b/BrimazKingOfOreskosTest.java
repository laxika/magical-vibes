package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrimazKingOfOreskosTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a vigilant Cat Soldier token tapped and attacking")
    void attackingCreatesCatSoldierToken() {
        addCreatureReady(player1, new BrimazKingOfOreskos());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Cat Soldier").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CAT, CardSubtype.SOLDIER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Blocking creates an untapped Cat Soldier token blocking that creature")
    void blockingCreatesCatSoldierTokenBlockingAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        addCreatureReady(player2, new BrimazKingOfOreskos());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        Permanent token = findPermanents(player2, "Cat Soldier").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isBlocking()).isTrue();
        assertThat(token.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(token.getBlockingTargets()).containsExactly(0);
        assertThat(gqs.isBlockedByAnyCreature(gd, attacker)).isTrue();
    }
}
