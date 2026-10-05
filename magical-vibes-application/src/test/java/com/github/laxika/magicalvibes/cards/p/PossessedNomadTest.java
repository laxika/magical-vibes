package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.cards.h.Hypochondria;
import com.github.laxika.magicalvibes.cards.n.NantukoShade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PossessedNomad.class, AvenTrooper.class, NantukoShade.class, Hypochondria.class})
class PossessedNomadTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 and becomes black at threshold")
    void thresholdBonus() {
        fillGraveyard(player1, 7);
        Permanent nomad = harness.addToBattlefieldAndReturn(player1, new PossessedNomad());

        assertThat(gqs.getEffectivePower(gd, nomad)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nomad)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, nomad)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Does not get threshold abilities below seven cards")
    void noThresholdBonus() {
        fillGraveyard(player1, 6);
        Permanent nomad = addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThat(gqs.getEffectivePower(gd, nomad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nomad)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, nomad, CardColor.BLACK)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Threshold counts only cards in the source controller's graveyard")
    void thresholdUsesControllerGraveyard() {
        fillGraveyard(player1, 6);
        fillGraveyard(player2, 7);
        Permanent nomad = addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThat(gqs.getEffectivePower(gd, nomad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nomad)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, nomad, CardColor.BLACK)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a target white creature at threshold")
    void destroysWhiteCreature() {
        fillGraveyard(player1, 7);
        addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aven Trooper");
        harness.assertInGraveyard(player2, "Aven Trooper");
    }

    @Test
    @DisplayName("Cannot target a nonwhite creature")
    void cannotTargetNonwhiteCreature() {
        fillGraveyard(player1, 7);
        addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NantukoShade());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a white noncreature permanent")
    void cannotTargetWhiteNoncreaturePermanent() {
        fillGraveyard(player1, 7);
        addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Hypochondria());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsThresholdWhileAlreadyOnBattlefield() {
        fillGraveyard(player1, 6);
        Permanent nomad = addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        assertThat(gqs.getEffectiveColors(gd, nomad)).containsExactly(CardColor.WHITE);

        fillGraveyard(player1, 7);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThat(gqs.getEffectivePower(gd, nomad)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nomad)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, nomad)).containsExactly(CardColor.BLACK);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Aven Trooper");
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        fillGraveyard(player1, 7);
        Permanent nomad = addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nomad.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Aven Trooper");
    }

    @Test
    void cannotActivateWhileTapped() {
        fillGraveyard(player1, 7);
        Permanent nomad = addReadyNomad();
        nomad.setTapped(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Aven Trooper");
    }

    @Test
    void losesThresholdBenefitsWhenGraveyardDropsBelowSeven() {
        fillGraveyard(player1, 7);
        Permanent nomad = addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        assertThat(gqs.getEffectiveColors(gd, nomad)).containsExactly(CardColor.BLACK);

        fillGraveyard(player1, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThat(gqs.getEffectivePower(gd, nomad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nomad)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, nomad)).containsExactly(CardColor.WHITE);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityResolvesAfterLosingThreshold() {
        fillGraveyard(player1, 7);
        Permanent nomad = addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(nomad.isTapped()).isTrue();

        fillGraveyard(player1, 6);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Aven Trooper");
        harness.assertNotOnBattlefield(player2, "Aven Trooper");
    }

    @Test
    void targetBecomingBlackBeforeResolutionIsNotDestroyed() {
        fillGraveyard(player1, 7);
        addReadyNomad();
        fillGraveyard(player2, 6);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PossessedNomad());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        fillGraveyard(player2, 7);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Possessed Nomad");
        harness.assertNotInGraveyard(player2, "Possessed Nomad");
    }

    @Test
    void canDestroyControllersWhiteCreature() {
        fillGraveyard(player1, 7);
        addReadyNomad();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AvenTrooper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Trooper");
        harness.assertNotOnBattlefield(player1, "Aven Trooper");
    }

    @Test
    void summoningSicknessPreventsTapAbility() {
        fillGraveyard(player1, 7);
        Permanent nomad = addReadyNomad();
        nomad.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Aven Trooper");
    }

    @Test
    void vigilanceAllowsAttackingWithoutTapping() {
        Permanent nomad = addReadyNomad();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(nomad.isAttacking()).isTrue();
        assertThat(nomad.isTapped()).isFalse();
    }

    private Permanent addReadyNomad() {
        Permanent nomad = harness.addToBattlefieldAndReturn(player1, new PossessedNomad());
        nomad.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return nomad;
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new AvenTrooper());
        }
        harness.setGraveyard(player, cards);
    }
}
