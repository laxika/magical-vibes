package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaughterOfTheDeep.class, GrizzlyBears.class})
class DaughterOfTheDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Merfolk token on its controller's second draw each turn")
    void createsTokenOnSecondDraw() {
        harness.addToBattlefieldAndReturn(player1, new DaughterOfTheDeep());
        setDeck(player1, 3);

        drawCard(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        drawCard(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Merfolk");
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates only one token for later draws in the same turn")
    void triggersOnlyOnSecondDraw() {
        harness.addToBattlefieldAndReturn(player1, new DaughterOfTheDeep());
        setDeck(player1, 4);

        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();
        drawCard(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Makes a target Merfolk unblockable for the turn")
    void makesTargetMerfolkUnblockable() {
        Permanent source = addReadyDaughter(player1);
        Permanent target = addReadyDaughter(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Merfolk creature")
    void cannotTargetNonMerfolk() {
        addReadyDaughter(player1);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Merfolk creature");
    }

    private Permanent addReadyDaughter(Player player) {
        return addReadyCreature(player, new DaughterOfTheDeep());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void setDeck(Player player, int count) {
        gd.playerDecks.get(player.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player.getId()).add(new GrizzlyBears());
        }
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
