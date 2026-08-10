package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeeperOfTheBeastsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 2/2 green Beast token when targeting an opponent with more creatures")
    void createsBeastToken() {
        Permanent keeper = readyKeeper(2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactly(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(keeper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Checks the creature-count condition only when activating")
    void creatureCountConditionIsCheckedOnlyOnActivation() {
        readyKeeper(2);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate when the opponent does not control more creatures")
    void cannotActivateWithoutMoreCreatures() {
        readyKeeper(1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        readyKeeper(2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent readyKeeper(int opponentCreatureCount) {
        Permanent keeper = addCreatureReady(player1, new KeeperOfTheBeasts());
        for (int i = 0; i < opponentCreatureCount; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }
        harness.addMana(player1, ManaColor.GREEN, 1);
        return keeper;
    }
}
