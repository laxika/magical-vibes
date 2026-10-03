package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NyxbornRollicker;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrimazKingOfOreskos.class, NyxbornRollicker.class})
class BrimazKingOfOreskosTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates an untapped vigilant Cat Soldier without declaring it as an attacker")
    void attackingCreatesCatSoldierToken() {
        Permanent brimaz = addCreatureReady(player1, new BrimazKingOfOreskos());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        Permanent token = findPermanents(player1, "Cat Soldier").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(brimaz.isTapped()).isFalse();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.isAttackedThisTurn()).isFalse();
        assertThat(token.getAttacksThisTurn()).isZero();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CAT, CardSubtype.SOLDIER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.VIGILANCE);
    }

    @Test
    @DisplayName("Blocking creates an untapped Cat Soldier token blocking that creature")
    void blockingCreatesCatSoldierTokenBlockingAttacker() {
        Permanent attacker = addCreatureReady(player1, new NyxbornRollicker());
        addCreatureReady(player2, new BrimazKingOfOreskos());

        declareAttackersAndPrepareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            resolveAllTriggers();
        });

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

    @Test
    @DisplayName("The blocking token is still created if the blocked attacker leaves before resolution")
    void createsTokenWhenBlockedAttackerLeaves() {
        Permanent attacker = addCreatureReady(player1, new NyxbornRollicker());
        addCreatureReady(player2, new BrimazKingOfOreskos());
        declareAttackersAndPrepareBlockers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            gd.playerBattlefields.get(player1.getId()).remove(attacker);
            gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player2, "Cat Soldier")).singleElement().satisfies(token -> {
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isBlocking()).isFalse();
        });
    }

    @Test
    @DisplayName("The blocking token still blocks the attacker after Brimaz leaves")
    void createsBlockingTokenWhenBrimazLeaves() {
        Permanent attacker = addCreatureReady(player1, new NyxbornRollicker());
        Permanent brimaz = addCreatureReady(player2, new BrimazKingOfOreskos());
        declareAttackersAndPrepareBlockers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            gd.playerBattlefields.get(player2.getId()).remove(brimaz);
            gd.playerGraveyards.get(player2.getId()).add(brimaz.getCard());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player2, "Cat Soldier")).singleElement().satisfies(token -> {
            assertThat(token.isTapped()).isFalse();
            assertThat(token.isBlocking()).isTrue();
            assertThat(token.getBlockingTargetIds()).containsExactly(attacker.getId());
        });
    }
}
