package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RebellionOfTheFlamekinTest extends BaseCardTest {

    private Permanent token() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elemental Shaman"))
                .findFirst().orElse(null);
    }

    // ===== Won clash — token gains haste =====

    @Test
    @DisplayName("Won clash: paying {1} creates a 3/1 Elemental Shaman with haste")
    void wonClashCreatesHasteToken() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addMana(player1, ManaColor.RED, 1);

        // Higher mana value on top for player1 (GrizzlyBears MV 2 > Forest MV 0) → player1 wins.
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.getTriggerCollectionService().performClash(gd, player1.getId());
        harness.passBothPriorities(); // resolve clash trigger → may-pay prompt
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = token();
        assertThat(token).isNotNull();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
    }

    // ===== Lost clash — token has no haste =====

    @Test
    @DisplayName("Lost clash: paying {1} creates the token but it has no haste")
    void lostClashCreatesTokenWithoutHaste() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addMana(player1, ManaColor.RED, 1);

        // Lower mana value on top for player1 (Forest MV 0 < GrizzlyBears MV 2) → player1 loses.
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());
        gd.playerDecks.get(player2.getId()).addFirst(new GrizzlyBears());

        harness.getTriggerCollectionService().performClash(gd, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = token();
        assertThat(token).isNotNull();
        assertThat(token.hasKeyword(Keyword.HASTE)).isFalse();
    }

    // ===== Declining pays nothing and makes no token =====

    @Test
    @DisplayName("Declining the {1} payment creates no token")
    void decliningCreatesNoToken() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addMana(player1, ManaColor.RED, 1);

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.getTriggerCollectionService().performClash(gd, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(token()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Granted haste wears off at cleanup =====

    @Test
    @DisplayName("Haste granted on a won clash wears off at end of turn")
    void hasteWearsOffAtCleanup() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new RebellionOfTheFlamekin());
        harness.addMana(player1, ManaColor.RED, 1);

        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).addFirst(new Forest());

        harness.getTriggerCollectionService().performClash(gd, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(token().hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(token().hasKeyword(Keyword.HASTE)).isFalse();
    }
}
