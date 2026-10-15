package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KraulWhipcracker.class, NoviceInspector.class})
class KraulWhipcrackerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a token an opponent controls")
    void etbDestroysOpponentsToken() {
        Permanent token = addToken(player2, true);
        castWhipcracker(List.of(token.getId()));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Soldier");
        harness.assertOnBattlefield(player1, "Kraul Whipcracker");
    }

    @Test
    @DisplayName("ETB cannot target a token you control")
    void etbCannotTargetOwnToken() {
        Permanent token = addToken(player1, true);

        assertThatThrownBy(() -> castWhipcracker(List.of(token.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a token an opponent controls");
    }

    @Test
    @DisplayName("ETB cannot target a nontoken permanent")
    void etbCannotTargetNontokenPermanent() {
        Permanent creature = addToken(player2, false);

        assertThatThrownBy(() -> castWhipcracker(List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a token an opponent controls");
    }

    @Test
    @DisplayName("ETB destroys an opponent's noncreature Clue token")
    void etbDestroysClueToken() {
        harness.enterBattlefieldAndReturn(player2, new NoviceInspector());
        resolveAllTriggers();
        Permanent clue = findPermanent(player2, "Clue");

        castWhipcracker(List.of(clue.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Clue");
        harness.assertOnBattlefield(player2, "Novice Inspector");
        harness.assertOnBattlefield(player1, "Kraul Whipcracker");
    }

    @Test
    @DisplayName("Creature resolves when there are no legal tokens to target")
    void resolvesWithoutLegalTargets() {
        addToken(player1, true);
        harness.addToBattlefield(player2, new NoviceInspector());

        castWhipcracker(List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Kraul Whipcracker");
        harness.assertOnBattlefield(player1, "Soldier");
        harness.assertOnBattlefield(player2, "Novice Inspector");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB does not destroy a token that changes to your control before resolution")
    void targetBecomesIllegalAfterControlChange() {
        Permanent token = addToken(player2, true);
        castWhipcracker(List.of(token.getId()));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kraul Whipcracker");
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player2.getId()).remove(token);
        gd.playerBattlefields.get(player1.getId()).add(token);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Soldier");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB still resolves after Kraul Whipcracker leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent token = addToken(player2, true);
        castWhipcracker(List.of(token.getId()));
        harness.passBothPriorities();
        Permanent whipcracker = findPermanent(player1, "Kraul Whipcracker");
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(whipcracker);
        gd.playerGraveyards.get(player1.getId()).add(whipcracker.getCard());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Soldier");
        harness.assertInGraveyard(player1, "Kraul Whipcracker");
    }
    private void castWhipcracker(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new KraulWhipcracker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, targetIds);
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player player, boolean token) {
        Card card = new Card();
        card.setName("Soldier");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(token);
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
