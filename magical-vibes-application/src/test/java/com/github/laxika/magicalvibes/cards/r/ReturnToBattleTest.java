package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GhostlyVisit;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReturnToBattle.class, ShuFootSoldiers.class, GhostlyVisit.class})
class ReturnToBattleTest extends BaseCardTest {

    @Test
    @DisplayName("Return to Battle returns target creature card from graveyard to hand")
    void returnsTargetCreatureFromGraveyardToHand() {
        Card creature = new ShuFootSoldiers();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ReturnToBattle()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Return to Battle returns only the targeted creature card")
    void returnsOnlyTheTargetedCreatureCard() {
        Card creature = new ShuFootSoldiers();
        Card noncreature = new GhostlyVisit();
        harness.setGraveyard(player1, List.of(noncreature, creature));
        harness.setHand(player1, List.of(new ReturnToBattle()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(noncreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Return to Battle cannot target noncreature card in graveyard")
    void cannotTargetNoncreatureCardInGraveyard() {
        Card noncreature = new GhostlyVisit();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new ReturnToBattle()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Return to Battle cannot target creature in opponent's graveyard")
    void cannotTargetCardInOpponentGraveyard() {
        Card creature = new ShuFootSoldiers();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ReturnToBattle()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Return to Battle fizzles if targeted creature leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card creature = new ShuFootSoldiers();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ReturnToBattle()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
