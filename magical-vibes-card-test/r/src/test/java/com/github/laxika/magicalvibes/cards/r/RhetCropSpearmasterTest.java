package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhetCropSpearmaster.class, LayClaim.class})
class RhetCropSpearmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadySpearmaster(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +1/+0 and first strike until end of turn")
    void exertBoostsAndGrantsFirstStrike() {
        Permanent spearmaster = addReadySpearmaster(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, spearmaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spearmaster)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spearmaster, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent spearmaster = addReadySpearmaster(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(spearmaster.isTapped()).isTrue();
        assertThat(spearmaster.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and grants no first strike")
    void decliningExertDoesNothing() {
        Permanent spearmaster = addReadySpearmaster(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, spearmaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spearmaster)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spearmaster, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(spearmaster.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert is paid before the bonus trigger resolves")
    void exertIsPaidBeforeBonusResolves() {
        Permanent spearmaster = addReadySpearmaster(player1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(spearmaster.getSkipUntapCount()).isPositive();
            assertThat(gqs.getEffectivePower(gd, spearmaster)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, spearmaster, Keyword.FIRST_STRIKE)).isFalse();
        });
    }

    @Test
    @DisplayName("Exert skips only the next controller untap step")
    void exertSkipsOnlyOneUntapStep() {
        Permanent spearmaster = addReadySpearmaster(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(spearmaster.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(spearmaster.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(spearmaster.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The boost and first strike expire at end of turn")
    void exertBonusExpiresAtEndOfTurn() {
        Permanent spearmaster = addReadySpearmaster(player1);
        harness.setLibrary(player2, List.of(new RhetCropSpearmaster()));

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, spearmaster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spearmaster)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, spearmaster, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Exert does not prevent untapping during a new controller's untap step")
    void exertRestrictionDoesNotFollowNewController() {
        Permanent spearmaster = addReadySpearmaster(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LayClaim()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.ensurePriority(player2);
        harness.castEnchantment(player2, 0, spearmaster.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spearmaster);
        harness.performUntapStep(player2);
        assertThat(spearmaster.isTapped()).isFalse();
    }

    private Permanent addReadySpearmaster(Player player) {
        return addCreatureReady(player, new RhetCropSpearmaster());
    }
}
