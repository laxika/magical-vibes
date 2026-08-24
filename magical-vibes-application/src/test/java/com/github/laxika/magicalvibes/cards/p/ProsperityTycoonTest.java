package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProsperityTycoon.class, GrizzlyBears.class})
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
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castTycoon();
        Permanent mercenary = findPermanents(player1, "Mercenary").getFirst();
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(mercenary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Mercenary token cannot target an opposing creature")
    void mercenaryCannotTargetOpposingCreature() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
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

    private Permanent castTycoon() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ProsperityTycoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Prosperity Tycoon");
    }
}
