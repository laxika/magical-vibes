package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectacularSpiderMan.class, GrizzlyBears.class})
class SpectacularSpiderManTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent spiderMan = addCreatureReady(player1, new SpectacularSpiderMan());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, spiderMan), 0, null, null);
        harness.passBothPriorities();

        assertThat(spiderMan.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spiderMan.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The sacrifice ability grants hexproof and indestructible to your creatures")
    void sacrificeGrantsHexproofAndIndestructibleToOwnCreatures() {
        Permanent spiderMan = addCreatureReady(player1, new SpectacularSpiderMan());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, spiderMan), 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spectacular Spider-Man");
        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(ally.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opposing.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(opposing.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(ally.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
