package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RhysticDeluge.class, PygmyRazorback.class, RhysticStudy.class})
class RhysticDelugeTest extends BaseCardTest {

    @Test
    @DisplayName("The target creature's controller is offered the payment")
    void targetControllerIsOfferedPayment() {
        addDeluge();
        Permanent target = addCreatureReady(player2, new PygmyRazorback());
        harness.addMana(player1, ManaColor.BLUE, 1);

        activate(target);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Paying {1} keeps the target creature untapped")
    void payingKeepsTargetCreatureUntapped() {
        addDeluge();
        Permanent target = addCreatureReady(player2, new PygmyRazorback());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        activate(target);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining the payment taps the target creature")
    void decliningTapsTargetCreature() {
        addDeluge();
        Permanent target = addCreatureReady(player2, new PygmyRazorback());
        harness.addMana(player1, ManaColor.BLUE, 1);

        activate(target);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped creature can still be targeted")
    void alreadyTappedCreatureCanBeTargeted() {
        addDeluge();
        Permanent target = addCreatureReady(player2, new PygmyRazorback());
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        activate(target);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The target creature is tapped when its controller cannot pay")
    void cannotPayTapsTargetCreature() {
        addDeluge();
        Permanent target = addCreatureReady(player2, new PygmyRazorback());
        harness.addMana(player1, ManaColor.BLUE, 1);

        activate(target);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addDeluge();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new RhysticStudy());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> activate(noncreature))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addDeluge() {
        harness.addToBattlefield(player1, new RhysticDeluge());
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
