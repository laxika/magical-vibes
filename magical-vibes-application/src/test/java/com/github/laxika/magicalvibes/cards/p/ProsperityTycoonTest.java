package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProsperityTycoon.class})
class ProsperityTycoonTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Mercenary token")
    void createsMercenaryToken() {
        castTycoon();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
        assertThat(findPermanents(player1, "Mercenary").getFirst().getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The Mercenary token boosts a creature you control at sorcery speed")
    void mercenaryBoostsCreatureYouControl() {
        Permanent creature = addCreatureReady(player1, new ProsperityTycoon());
        castTycoon();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Mercenary token cannot target an opposing creature")
    void mercenaryCannotTargetOpposingCreature() {
        Permanent opposingCreature = addCreatureReady(player2, new ProsperityTycoon());
        castTycoon();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, mercenaryIndex, 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Sacrificing a token grants indestructible and taps Prosperity Tycoon")
    void sacrificingTokenGrantsIndestructibleAndTapsTycoon() {
        Permanent tycoon = castTycoon();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int tycoonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tycoon);
        harness.activateAbility(player1, tycoonIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, tycoon, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(tycoon.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mercenary);
    }

    @Test
    @DisplayName("The indestructible ability wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent tycoon = castTycoon();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int tycoonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tycoon);
        harness.activateAbility(player1, tycoonIndex, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, tycoon, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, tycoon, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The token is sacrificed as a cost, while tapping and indestructible wait for resolution")
    void sacrificeIsPaidBeforeResolution() {
        Permanent tycoon = castTycoon();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tycoon), 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mercenary);
        assertThat(tycoon.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, tycoon, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(tycoon.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, tycoon, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A tapped Tycoon can activate its protection ability during combat")
    void tappedTycoonCanActivateDuringCombat() {
        Permanent tycoon = castTycoon();
        tycoon.tap();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(tycoon), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, tycoon, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(tycoon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A nontoken creature cannot pay the token sacrifice cost")
    void cannotActivateWithoutToken() {
        Permanent tycoon = harness.addToBattlefieldAndReturn(player1, new ProsperityTycoon());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tycoon);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, tycoon, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The Mercenary cannot activate during combat")
    void mercenaryCannotActivateDuringCombat() {
        Permanent tycoon = castTycoon();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mercenary), 0, null, tycoon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The newly created Mercenary cannot pay its tap cost")
    void mercenaryCannotActivateWithSummoningSickness() {
        Permanent tycoon = castTycoon();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mercenary), 0, null, tycoon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenary.isTapped()).isFalse();
        assertThat(tycoon.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The Mercenary boost wears off at end of turn")
    void mercenaryBoostWearsOffAtEndOfTurn() {
        Permanent tycoon = castTycoon();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mercenary),
                0, null, tycoon.getId());
        harness.passBothPriorities();
        assertThat(tycoon.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tycoon.getPowerModifier()).isZero();
    }

    private Permanent castTycoon() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ProsperityTycoon(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Prosperity Tycoon");
    }
}
