package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninSnarecaster.class, TravelingPhilosopher.class})
class LeoninSnarecasterTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability taps the target creature")
    void acceptingTapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());

        castLeoninSnarecaster();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the target creature untapped")
    void decliningLeavesTargetUntapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());

        castLeoninSnarecaster();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The entering Snarecaster can target and tap itself")
    void canTapItself() {
        castLeoninSnarecaster();
        harness.passBothPriorities();
        Permanent snarecaster = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, snarecaster.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(snarecaster.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped creature is a legal target and stays tapped")
    void canTargetTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        target.tap();

        castLeoninSnarecaster();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does not resolve if its target leaves the battlefield")
    void targetLeavingPreventsResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());

        castLeoninSnarecaster();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability can tap another friendly creature after Snarecaster leaves")
    void sourceLeavingDoesNotPreventTappingFriendlyCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());

        castLeoninSnarecaster();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent snarecaster = gd.playerBattlefields.get(player1.getId()).getLast();
        gd.playerBattlefields.get(player1.getId()).remove(snarecaster);
        gd.playerGraveyards.get(player1.getId()).add(snarecaster.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void castLeoninSnarecaster() {
        harness.setHand(player1, List.of(new LeoninSnarecaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }
}
