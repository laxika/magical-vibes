package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TeysaOfTheGhostCouncil.class)
class TeysaOfTheGhostCouncilTest extends BaseCardTest {

    @Test
    void entersWithSpiritTokenAndIntensifies() {
        Permanent teysa = harness.enterBattlefieldAndReturn(player1, new TeysaOfTheGhostCouncil());
        harness.passBothPriorities();

        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .findFirst()
                .orElseThrow();

        assertThat(gd.getCardIntensity(teysa.getCard().getId())).isEqualTo(1);
        assertThat(spirit.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
    }

    @Test
    void mayExileAtEndStepAndReturnsAtItsNextUpkeepWithHaste() {
        harness.addToBattlefield(player1, new TeysaOfTheGhostCouncil());

        exileAtEndStep(true);

        assertThat(findTeysa(player1)).isNull();
        runUpkeepOf(player2);
        assertThat(findTeysa(player1)).isNull();

        runUpkeepOf(player1);

        Permanent returned = findTeysa(player1);
        assertThat(returned).isNotNull();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void decliningEndStepAbilityLeavesTeysaOnTheBattlefield() {
        harness.addToBattlefield(player1, new TeysaOfTheGhostCouncil());

        exileAtEndStep(false);

        assertThat(findTeysa(player1)).isNotNull();
    }

    private void exileAtEndStep(boolean accept) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
        harness.clearPriorityPassed();
    }

    private void runUpkeepOf(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findTeysa(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Teysa of the Ghost Council"))
                .findFirst()
                .orElse(null);
    }
}
