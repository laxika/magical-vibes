package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouExistOnlyToAmuse.class, Forest.class, GrizzlyBears.class, SerraAngel.class})
class YouExistOnlyToAmuseTest extends BaseCardTest {

    private static final String DEVIL_MODE = "Create three 1/1 red Devil creature tokens";
    private static final String WEAKEN_MODE =
            "Opposing creatures have base power and toughness 1/1 and lose all abilities until your next turn";

    @Test
    @DisplayName("The Devil mode creates three Devils with the damage death ability")
    void createsDevils() {
        resolveScheme();
        chooseModes(DEVIL_MODE);

        assertThat(findPermanents(player1, "Devil")).hasSize(3);
    }

    @Test
    @DisplayName("The second mode makes opposing creatures abilityless 1/1s until your next turn")
    void weakensOpposingCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new SerraAngel());
        int originalPower = gqs.getEffectivePower(gd, opposingCreature);
        int originalToughness = gqs.getEffectiveToughness(gd, opposingCreature);

        resolveScheme();
        chooseModes(WEAKEN_MODE);

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);

        endTurn(player1);
        endTurn(player2);

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(originalToughness);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("With six lands, both modes may be chosen")
    void sixLandsAllowBothModes() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveScheme();
        chooseModes(DEVIL_MODE, WEAKEN_MODE);

        assertThat(findPermanents(player1, "Devil")).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(1);
    }

    private void resolveScheme() {
        YouExistOnlyToAmuse scheme = new YouExistOnlyToAmuse();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }

    private void chooseModes(String... modes) {
        for (String mode : modes) {
            harness.handleListChoice(player1, mode);
        }
        if (gd.interaction.isAwaitingInput()) {
            harness.handleListChoice(player1, ChooseOneEffect.FINISH_MODE_SELECTION);
        }
        resolveAllTriggers();
    }

    private void endTurn(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.CLEANUP);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isNotEqualTo(activePlayer.getId());
    }
}
