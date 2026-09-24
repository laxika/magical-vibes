package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedGhostIntangibleGenius.class, GrizzlyBears.class})
class RedGhostIntangibleGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates a hasty red Ape Villain token")
    void secondDrawCreatesHastyApeToken() {
        harness.addToBattlefield(player1, new RedGhostIntangibleGenius());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ape")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getColor() == CardColor.RED
                        && permanent.getCard().getPower() == 3
                        && permanent.getCard().getToughness() == 3
                        && permanent.getCard().getSubtypes().contains(CardSubtype.APE)
                        && permanent.getCard().getSubtypes().contains(CardSubtype.VILLAIN)
                        && permanent.getCard().getKeywords().contains(Keyword.HASTE));

        drawCard(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Red Ghost can't be blocked")
    void cannotBeBlocked() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent ghost = addCreatureReady(player1, new RedGhostIntangibleGenius());
        ghost.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ghost);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private void drawCard(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
