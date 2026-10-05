package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nighthaze.class, NestInvader.class, Forest.class, Swamp.class})
class NighthazeTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains swampwalk and the spell's controller draws a card")
    void grantsSwampwalkAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(new Nighthaze()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Granted swampwalk wears off at end of turn")
    void swampwalkWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NestInvader());
        harness.setHand(player1, List.of(new Nighthaze()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Nighthaze()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not draw when the only target leaves before resolution")
    void doesNotDrawWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NestInvader());
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(new Nighthaze()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Nighthaze");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new Nighthaze()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted swampwalk prevents blocking when the defender controls a Swamp")
    void swampwalkPreventsBlocking() {
        Permanent attacker = addCreatureReady(player1, new NestInvader());
        Permanent blocker = addCreatureReady(player2, new NestInvader());
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new Nighthaze()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, attacker.getId());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A Swamp controlled only by the attacker does not prevent blocking")
    void swampwalkAllowsBlockingWithoutDefendersSwamp() {
        Permanent attacker = addCreatureReady(player1, new NestInvader());
        harness.addToBattlefield(player1, new Swamp());
        Permanent blocker = addCreatureReady(player2, new NestInvader());
        harness.setHand(player1, List.of(new Nighthaze()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, attacker.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
