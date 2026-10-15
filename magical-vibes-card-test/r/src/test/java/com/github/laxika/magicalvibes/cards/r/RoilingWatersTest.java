package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MakindiAeronaut;
import com.github.laxika.magicalvibes.cards.n.NotionThief;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoilingWaters.class, MakindiAeronaut.class, NotionThief.class})
class RoilingWatersTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two opponent creatures and makes the target player draw two cards")
    void returnsCreaturesAndDrawsCards() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.castSorcery(player1, 0, List.of(player2.getId(), first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Makindi Aeronaut");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 4);
    }

    @Test
    @DisplayName("Allows returning no creatures while still drawing two cards")
    void allowsNoCreatureTargets() {
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.castSorcery(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 2);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the caster")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MakindiAeronaut());
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(player2.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void returnsOneCreatureAndLetsCasterDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        harness.setLibrary(player1, List.of(new MakindiAeronaut(), new MakindiAeronaut()));
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of(player1.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Makindi Aeronaut");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature.getCard());
    }

    @Test
    void returnsNotionThiefBeforeDrawing() {
        Permanent thief = harness.addToBattlefieldAndReturn(player2, new NotionThief());
        harness.setLibrary(player1, List.of(new MakindiAeronaut(), new MakindiAeronaut()));
        harness.setLibrary(player2, List.of(new MakindiAeronaut(), new MakindiAeronaut()));
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of(player1.getId(), thief.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Notion Thief");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(thief.getCard());
    }

    @Test
    void stillDrawsWhenAllCreatureTargetsLeaveBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        harness.setLibrary(player1, List.of(new MakindiAeronaut(), new MakindiAeronaut()));
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of(player1.getId(), first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(first, second));
        harness.setGraveyard(player2, List.of(first.getCard(), second.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first.getCard(), second.getCard());
    }

    @Test
    void skipsCreatureThatIsNoLongerControlledByOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        harness.setLibrary(player1, List.of(new MakindiAeronaut(), new MakindiAeronaut()));
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of(player1.getId(), first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerBattlefields.get(player1.getId()).add(first);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first);
        harness.assertNotOnBattlefield(player2, "Makindi Aeronaut");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(second.getCard());
    }

    @Test
    void cannotChooseSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(player1.getId(), creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new MakindiAeronaut());
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(player1.getId(), first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsCreatureToOwnerRatherThanController() {
        MakindiAeronaut card = new MakindiAeronaut();
        card.setOwnerId(player1.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        harness.setLibrary(player2, List.of(new MakindiAeronaut(), new MakindiAeronaut()));
        harness.setHand(player1, List.of(new RoilingWaters()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Makindi Aeronaut");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2).doesNotContain(card);
    }
}
