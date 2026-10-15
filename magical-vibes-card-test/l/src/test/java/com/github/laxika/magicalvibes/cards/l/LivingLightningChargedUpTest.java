package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.w.WarriorsOfWakanda;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingLightningChargedUp.class, WarriorsOfWakanda.class})
class LivingLightningChargedUpTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, another creature gets +1/+0 and haste")
    void boostsAnotherCreatureAndGrantsHaste() {
        addLivingLightning();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability can target only another creature controlled by its controller")
    void targetsOnlyAnotherCreatureYouControl() {
        Permanent source = addLivingLightning();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new WarriorsOfWakanda());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownCreature.getId())
                .doesNotContain(source.getId(), opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The temporary boost and haste wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        addLivingLightning();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addLivingLightning();
        harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("With no other creature, the ability has no legal target")
    void noOtherCreatureLeavesNoTriggerOnStack() {
        addLivingLightning();

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The combat ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = addLivingLightning();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A target that changes to the opponent's control receives neither effect")
    void targetMustRemainUnderYourControlAtResolution() {
        addLivingLightning();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WarriorsOfWakanda());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    private Permanent addLivingLightning() {
        return harness.addToBattlefieldAndReturn(player1, new LivingLightningChargedUp());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
