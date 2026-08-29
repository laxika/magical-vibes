package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ApocalypseChime;
import com.github.laxika.magicalvibes.cards.e.EbonyRhino;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainStasis.class, EbonyRhino.class, ApocalypseChime.class})
class ChainStasisTest extends BaseCardTest {

    @Test
    @DisplayName("Taps an untapped target creature")
    void tapsUntappedCreature() {
        Permanent target = addCreatureReady(player2, new EbonyRhino());
        harness.setHand(player1, List.of(new ChainStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps a tapped target creature")
    void untapsTappedCreature() {
        Permanent target = addCreatureReady(player1, new EbonyRhino());
        target.tap();
        harness.setHand(player1, List.of(new ChainStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The target creature's controller — not the caster — is offered the {2}{U} copy payment")
    void targetControllerIsOfferedThePayment() {
        Permanent target = addCreatureReady(player2, new EbonyRhino());
        harness.setHand(player1, List.of(new ChainStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Declining the payment leaves no copy on the stack")
    void decliningCreatesNoCopy() {
        Permanent target = addCreatureReady(player2, new EbonyRhino());
        harness.setHand(player1, List.of(new ChainStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Paying {2}{U} puts a copy controlled by the target's controller on the stack")
    void payingCreatesCopyForTargetController() {
        Permanent target = addCreatureReady(player2, new EbonyRhino());
        harness.setHand(player1, List.of(new ChainStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.stack.getLast().isCopy()).isTrue();
    }

    @Test
    @DisplayName("Accepting without the mana to pay creates no copy")
    void acceptingWithoutManaCreatesNoCopy() {
        Permanent target = addCreatureReady(player2, new EbonyRhino());
        harness.setHand(player1, List.of(new ChainStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The caster may decline the initial tap-or-untap instruction")
    void casterMayDeclineInitialTapOrUntap() {
        Permanent target = addCreatureReady(player2, new EbonyRhino());
        harness.setHand(player1, List.of(new ChainStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The copied spell may be retargeted to another creature")
    void copiedSpellMayChooseAnotherTarget() {
        Permanent firstTarget = addCreatureReady(player2, new EbonyRhino());
        Permanent secondTarget = addCreatureReady(player2, new EbonyRhino());
        harness.setHand(player1, List.of(new ChainStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(firstTarget.isTapped()).isTrue();
        assertThat(secondTarget.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new EbonyRhino());
        Permanent nonCreature = new Permanent(new ApocalypseChime());
        gd.playerBattlefields.get(player2.getId()).add(nonCreature);
        harness.setHand(player1, List.of(new ChainStasis()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
