package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NimanaSkitterSneak.class})
class NimanaSkitterSneakTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats without enough cards in an opponent's graveyard")
    void baseStatsBelowThreshold() {
        fillGraveyard(player2, 7);
        harness.addToBattlefield(player1, new NimanaSkitterSneak());

        assertStats(3, 4);
        assertThat(gqs.hasKeyword(gd, findSneak(), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and menace when an opponent has eight cards in their graveyard")
    void boostAtThreshold() {
        fillGraveyard(player2, 8);
        harness.addToBattlefield(player1, new NimanaSkitterSneak());

        assertStats(4, 4);
        assertThat(gqs.hasKeyword(gd, findSneak(), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The controller's graveyard does not satisfy the condition")
    void ownGraveyardDoesNotCount() {
        fillGraveyard(player1, 8);
        harness.addToBattlefield(player1, new NimanaSkitterSneak());

        assertStats(3, 4);
        assertThat(gqs.hasKeyword(gd, findSneak(), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when the opponent's graveyard drops below eight cards")
    void losesBonusWhenGraveyardShrinks() {
        fillGraveyard(player2, 8);
        harness.addToBattlefield(player1, new NimanaSkitterSneak());
        Permanent sneak = findSneak();

        assertStats(4, 4);
        assertThat(gqs.hasKeyword(gd, sneak, Keyword.MENACE)).isTrue();

        gd.playerGraveyards.get(player2.getId()).removeFirst();

        assertStats(3, 4);
        assertThat(gqs.hasKeyword(gd, sneak, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Gains the bonus immediately when the opponent's graveyard reaches eight")
    void gainsBonusWhenGraveyardGrows() {
        fillGraveyard(player2, 7);
        Permanent sneak = harness.addToBattlefieldAndReturn(player1, new NimanaSkitterSneak());

        assertStats(3, 4);
        assertThat(gqs.hasKeyword(gd, sneak, Keyword.MENACE)).isFalse();

        gd.playerGraveyards.get(player2.getId()).add(new NimanaSkitterSneak());

        assertStats(4, 4);
        assertThat(gqs.hasKeyword(gd, sneak, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Cards in separate graveyards are not combined to reach eight")
    void doesNotCombineGraveyards() {
        fillGraveyard(player1, 4);
        fillGraveyard(player2, 4);
        Permanent sneak = harness.addToBattlefieldAndReturn(player1, new NimanaSkitterSneak());

        assertStats(3, 4);
        assertThat(gqs.hasKeyword(gd, sneak, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("More than eight cards grants the bonus only to the qualifying controller's creature")
    void aboveThresholdDoesNotBoostOpposingCreature() {
        fillGraveyard(player2, 10);
        Permanent sneak = harness.addToBattlefieldAndReturn(player1, new NimanaSkitterSneak());
        Permanent opposingSneak = harness.addToBattlefieldAndReturn(player2, new NimanaSkitterSneak());

        assertStats(4, 4);
        assertThat(gqs.hasKeyword(gd, sneak, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingSneak)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingSneak)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opposingSneak, Keyword.MENACE)).isFalse();
    }
    @Test
    @DisplayName("At the threshold one blocker is illegal but two blockers are legal")
    void menaceRequiresTwoBlockers() {
        fillGraveyard(player2, 8);
        addCreatureReady(player1, new NimanaSkitterSneak());
        Permanent first = addCreatureReady(player2, new NimanaSkitterSneak());
        Permanent second = addCreatureReady(player2, new NimanaSkitterSneak());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Below the threshold a single creature can block")
    void singleBlockerLegalBelowThreshold() {
        fillGraveyard(player2, 7);
        addCreatureReady(player1, new NimanaSkitterSneak());
        Permanent blocker = addCreatureReady(player2, new NimanaSkitterSneak());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        assertThat(blocker.isBlocking()).isTrue();
    }
    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new NimanaSkitterSneak());
        }
        harness.setGraveyard(player, cards);
    }

    private Permanent findSneak() {
        return findPermanent(player1, "Nimana Skitter-Sneak");
    }

    private void assertStats(int power, int toughness) {
        Permanent sneak = findSneak();
        assertThat(gqs.getEffectivePower(gd, sneak)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, sneak)).isEqualTo(toughness);
    }
}
