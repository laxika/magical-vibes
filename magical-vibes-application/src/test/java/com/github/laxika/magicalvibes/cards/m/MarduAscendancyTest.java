package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarduAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature attack creates a tapped and attacking Goblin")
    void nontokenCreatureAttackCreatesGoblin() {
        addReadyAscendancy();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent goblin = findPermanent(player1, "Goblin");
        assertThat(goblin.getCard().isToken()).isTrue();
        assertThat(goblin.isTapped()).isTrue();
        assertThat(goblin.isAttackedThisTurn()).isTrue();
        assertThat(goblin.getEffectivePower()).isEqualTo(1);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A token creature attack does not trigger Mardu Ascendancy")
    void tokenCreatureAttackDoesNotTrigger() {
        addReadyAscendancy();
        addCreatureReady(player1, createTokenCreature());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing Mardu Ascendancy gives your creatures +0/+3 until end of turn")
    void sacrificeBoostsOwnCreatures() {
        addReadyAscendancy();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof MarduAscendancy);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MarduAscendancy);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        assertThat(enemy.getEffectivePower()).isEqualTo(2);
        assertThat(enemy.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyAscendancy() {
        return harness.addToBattlefieldAndReturn(player1, new MarduAscendancy());
    }

    private Card createTokenCreature() {
        Card token = new Card();
        token.setName("Test Goblin");
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setColor(CardColor.RED);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
