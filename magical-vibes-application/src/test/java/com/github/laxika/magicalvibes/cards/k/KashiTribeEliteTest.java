package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SkeletalSnake;
import com.github.laxika.magicalvibes.cards.s.SosukeSonOfSeshiro;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KashiTribeEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary Snakes you control have shroud")
    void grantsShroudToLegendarySnakesYouControl() {
        Permanent kashi = addReady(player1, new KashiTribeElite());
        Permanent sosuke = addReady(player1, new SosukeSonOfSeshiro());
        Permanent ordinarySnake = addReady(player1, new SkeletalSnake());
        Permanent opposingSosuke = addReady(player2, new SosukeSonOfSeshiro());

        assertThat(gqs.hasKeyword(gd, sosuke, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, kashi, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, ordinarySnake, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSosuke, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents targeting a legendary Snake")
    void cannotBeTargetedBySpells() {
        addReady(player1, new KashiTribeElite());
        Permanent sosuke = addReady(player1, new SosukeSonOfSeshiro());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, sosuke.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksDamagedCreature() {
        Permanent kashi = addReady(player1, new KashiTribeElite());
        kashi.setAttacking(true);
        addReady(player2, new GiantSpider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveStack();

        Permanent spider = findPermanent(player2, "Giant Spider");
        assertThat(spider.isTapped()).isTrue();
        assertThat(spider.getSkipUntapCount()).isEqualTo(1);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void resolveStack() {
        for (int guard = 0; guard < 40 && !gd.stack.isEmpty() && !gd.interaction.isAwaitingInput(); guard++) {
            harness.passBothPriorities();
        }
    }
}
