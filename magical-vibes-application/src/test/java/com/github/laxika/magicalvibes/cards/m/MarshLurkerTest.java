package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarshLurker.class, Forest.class, Swamp.class})
class MarshLurkerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Swamp grants fear until end of turn")
    void sacrificingSwampGrantsFear() {
        Permanent lurker = addCreatureReady(player1, new MarshLurker());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lurker, Keyword.FEAR)).isTrue();
        harness.assertInGraveyard(player1, "Swamp");
    }

    @Test
    @DisplayName("Fear wears off at end of turn")
    void fearWearsOffAtEndOfTurn() {
        Permanent lurker = addCreatureReady(player1, new MarshLurker());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lurker, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a Swamp to sacrifice")
    void requiresSwampToSacrifice() {
        addCreatureReady(player1, new MarshLurker());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate using an opponent's Swamp")
    void cannotSacrificeOpponentsSwamp() {
        addCreatureReady(player1, new MarshLurker());
        harness.addToBattlefield(player2, new Swamp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Swamp");
    }
}
