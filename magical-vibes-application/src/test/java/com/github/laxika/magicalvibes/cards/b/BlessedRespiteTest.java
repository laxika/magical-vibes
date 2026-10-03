package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlessedRespite.class, GrizzlyBears.class, GiantSpider.class})
class BlessedRespiteTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles the target player's graveyard into their library")
    void shufflesTargetPlayersGraveyard() {
        Card bear = new GrizzlyBears();
        Card spider = new GiantSpider();
        harness.setGraveyard(player2, List.of(bear, spider));
        harness.setHand(player1, List.of(new BlessedRespite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize + 2);
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .contains(bear.getId(), spider.getId());
    }

    @Test
    @DisplayName("Prevents all combat damage for the turn")
    void preventsAllCombatDamage() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BlessedRespite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlessedRespite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target its controller without shuffling the resolving spell")
    void shufflesOwnGraveyardWithoutTheResolvingSpell() {
        Card bear = new GrizzlyBears();
        Card opposingSpider = new GiantSpider();
        Card respite = new BlessedRespite();
        harness.setGraveyard(player1, List.of(bear));
        harness.setGraveyard(player2, List.of(opposingSpider));
        harness.setHand(player1, List.of(respite));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize + 1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(bear).doesNotContain(respite);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(respite);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingSpider);
    }

    @Test
    @DisplayName("Shuffles cards that entered the graveyard after the spell was cast")
    void shufflesGraveyardContentsAtResolution() {
        Card bear = new GrizzlyBears();
        Card spider = new GiantSpider();
        harness.setGraveyard(player2, List.of(bear));
        harness.setHand(player1, List.of(new BlessedRespite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.setGraveyard(player2, List.of(bear, spider));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize + 2).contains(bear, spider);
    }

    @Test
    @DisplayName("Prevents combat damage to both attacking and blocking creatures")
    void preventsCombatDamageToCreatures() {
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new BlessedRespite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
    }
}
