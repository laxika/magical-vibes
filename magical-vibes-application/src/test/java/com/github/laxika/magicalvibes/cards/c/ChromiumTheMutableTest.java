package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChromiumTheMutableTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card transforms Chromium until end of turn")
    void discardTransformsChromium() {
        Permanent chromium = addChromium(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.isUncounterable(gd, chromium.getCard())).isTrue();
        activateChromium(chromium);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.effectiveCreatureSubtypes(gd, chromium)).containsExactly(CardSubtype.HUMAN);
        assertThat(gqs.getEffectivePower(gd, chromium)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chromium)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.HEXPROOF)).isTrue();
        assertThat(chromium.isCantBeBlocked()).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, chromium)).isEmpty();
    }

    @Test
    @DisplayName("Chromium's transformation wears off at end of turn")
    void transformationWearsOffAtEndOfTurn() {
        Permanent chromium = addChromium(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        activateChromium(chromium);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, chromium)).doesNotContain(CardSubtype.HUMAN);
        assertThat(gqs.getEffectivePower(gd, chromium)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, chromium)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.HEXPROOF)).isFalse();
        assertThat(chromium.isCantBeBlocked()).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, chromium)).hasSize(1);
    }

    private void activateChromium(Permanent chromium) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chromium), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addChromium(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ChromiumTheMutable());
    }
}
