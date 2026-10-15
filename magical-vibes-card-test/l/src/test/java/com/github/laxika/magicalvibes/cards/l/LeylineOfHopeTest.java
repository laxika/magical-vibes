package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.PatchedPlaything;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfHope.class, PatchedPlaything.class})
class LeylineOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in the opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfHope()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        openingHarness.assertOnBattlefield(openingHarness.getPlayer1(), "Leyline of Hope");
        openingHarness.assertNotInHand(openingHarness.getPlayer1(), "Leyline of Hope");
    }

    @Test
    @DisplayName("Leyline adds one life to each life-gain event for its controller")
    void addsOneLifeToControllerLifeGain() {
        harness.addToBattlefield(player1, new LeylineOfHope());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Leyline does not modify an opponent's life gain")
    void doesNotModifyOpponentsLifeGain() {
        harness.addToBattlefield(player1, new LeylineOfHope());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Own creatures get +2/+2 at least seven life above starting life")
    void buffsOwnCreaturesAtLifeThreshold() {
        harness.addToBattlefield(player1, new LeylineOfHope());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PatchedPlaything());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new PatchedPlaything());

        harness.setLife(player1, 26);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);

        harness.setLife(player1, 27);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(3);
    }

    @Test
    void mayDeclineOpeningHandPlacement() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfHope()));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        openingHarness.assertInHand(openingHarness.getPlayer1(), "Leyline of Hope");
        openingHarness.assertNotOnBattlefield(openingHarness.getPlayer1(), "Leyline of Hope");
    }

    @Test
    void multipleCopiesAddOneLifeEachPerEvent() {
        harness.addToBattlefield(player1, new LeylineOfHope());
        harness.addToBattlefield(player1, new LeylineOfHope());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        harness.assertLife(player1, 25);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.assertLife(player1, 28);
    }

    @Test
    void gainingZeroLifeDoesNotBecomeOneLife() {
        harness.addToBattlefield(player1, new LeylineOfHope());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 0));

        harness.assertLife(player1, 20);
    }

    @Test
    void bonusDisappearsWhenLifeFallsBelowThreshold() {
        harness.addToBattlefield(player1, new LeylineOfHope());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PatchedPlaything());
        harness.setLife(player1, 27);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 1, null));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void commanderBonusRequiresSevenLifeAboveForty() {
        gd.format = DeckFormat.COMMANDER;
        harness.addToBattlefield(player1, new LeylineOfHope());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PatchedPlaything());

        harness.setLife(player1, 46);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.setLife(player1, 47);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }
}
