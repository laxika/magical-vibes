package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.ImplementsOfSacrifice;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrostaniThreeWhispers.class, RagingGoblin.class, ImplementsOfSacrifice.class})
class TrostaniThreeWhispersTest extends BaseCardTest {

    @Test
    @DisplayName("The abilities grant their respective keywords until end of turn")
    void grantsKeywordsUntilEndOfTurn() {
        addTrostani();
        Permanent target = addCreatureReady(player1, new RagingGoblin());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The hybrid ability can be paid with either color")
    void hybridAbilityUsesGreenOrWhiteMana() {
        addTrostani();
        Permanent greenTarget = addCreatureReady(player1, new RagingGoblin());
        Permanent whiteTarget = addCreatureReady(player1, new RagingGoblin());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 1, null, greenTarget.getId());
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, null, whiteTarget.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, greenTarget, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, whiteTarget, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The abilities cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addTrostani();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ImplementsOfSacrifice());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Each ability can target Trostani while she is tapped and summoning sick")
    void canTargetSelfWhileTappedAndSummoningSick(int abilityIndex) {
        Permanent trostani = harness.addToBattlefieldAndReturn(player1, new TrostaniThreeWhispers());
        trostani.setSummoningSick(true);
        trostani.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, abilityIndex, null, trostani.getId());
        assertThat(gqs.hasKeyword(gd, trostani, grantedKeyword(abilityIndex))).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trostani, grantedKeyword(abilityIndex))).isTrue();
        assertThat(trostani.isTapped()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @DisplayName("Each ability can target an opponent's creature during that opponent's turn")
    void canTargetOpponentCreatureOnOpponentTurn(int abilityIndex) {
        addTrostani();
        Permanent target = addCreatureReady(player2, new TrostaniThreeWhispers());
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, grantedKeyword(abilityIndex))).isTrue();
    }

    private Keyword grantedKeyword(int abilityIndex) {
        return switch (abilityIndex) {
            case 0 -> Keyword.DEATHTOUCH;
            case 1 -> Keyword.VIGILANCE;
            case 2 -> Keyword.DOUBLE_STRIKE;
            default -> throw new IllegalArgumentException("Unknown ability index");
        };
    }

    private void addTrostani() {
        addCreatureReady(player1, new TrostaniThreeWhispers());
    }
}
