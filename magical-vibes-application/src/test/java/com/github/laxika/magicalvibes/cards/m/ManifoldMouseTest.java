package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.MouseTrapper;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheSpade;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManifoldMouse.class, MouseTrapper.class, DruidOfTheSpade.class})
class ManifoldMouseTest extends BaseCardTest {

    private static final String DOUBLE_STRIKE_MODE = "Double strike";
    private static final String TRAMPLE_MODE = "Trample";

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new ManifoldMouse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    void beginningOfCombatGrantsChosenKeywordToTargetMouse() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ManifoldMouse());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MouseTrapper());

        advanceToCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, DOUBLE_STRIKE_MODE);

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, source, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void chosenKeywordWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ManifoldMouse());

        advanceToCombat();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, TRAMPLE_MODE);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void cannotTargetNonMouseOrOpponentMouse() {
        harness.addToBattlefield(player1, new ManifoldMouse());
        Permanent nonMouse = harness.addToBattlefieldAndReturn(player1, new DruidOfTheSpade());
        Permanent opponentMouse = harness.addToBattlefieldAndReturn(player2, new MouseTrapper());

        advanceToCombat();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonMouse.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentMouse.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    void offspringDoesNotCreateTokenWhenNotPaid() {
        harness.setHand(player1, List.of(new ManifoldMouse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void combatAbilityDoesNotTriggerOnOpponentsTurn() {
        Permanent mouse = harness.addToBattlefieldAndReturn(player1, new ManifoldMouse());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, mouse, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, mouse, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void offspringCopyAlsoTriggersAndCanGrantTheOtherKeyword() {
        harness.setHand(player1, List.of(new ManifoldMouse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        advanceToCombat();
        harness.handlePermanentChosen(player1, token.getId());
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, DOUBLE_STRIKE_MODE);
        harness.passBothPriorities();
        harness.handleListChoice(player1, TRAMPLE_MODE);

        assertThat(gqs.hasKeyword(gd, token, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }
}
