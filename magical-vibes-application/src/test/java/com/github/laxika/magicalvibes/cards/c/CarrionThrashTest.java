package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Blightning;
import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarrionThrash.class, CavernThoctar.class, DregscapeZombie.class, Blightning.class})
class CarrionThrashTest extends BaseCardTest {

    private void killInCombat() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new CarrionThrash());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new CavernThoctar());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Carrion Thrash");
    }

    private void chooseTargetAndResolveUntilPayment(Card target) {
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choose another creature before resolution, then pay to return it")
    void diesPayReturnsAnotherCreature() {
        DregscapeZombie target = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(target));
        killInCombat();
        chooseTargetAndResolveUntilPayment(target);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Dregscape Zombie");
        harness.assertNotInGraveyard(player1, "Dregscape Zombie");
        harness.assertInGraveyard(player1, "Carrion Thrash");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Declining payment leaves the chosen creature in the graveyard")
    void diesDeclineReturnsNothing() {
        DregscapeZombie target = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(target));
        killInCombat();
        chooseTargetAndResolveUntilPayment(target);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Dregscape Zombie");
        harness.assertNotInHand(player1, "Dregscape Zombie");
    }

    @Test
    @DisplayName("Without another creature card there is no target and no payment prompt")
    void diesWithNoOtherCreatureCard() {
        harness.setGraveyard(player1, List.of(new Blightning()));
        killInCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Carrion Thrash");
        harness.assertNotInHand(player1, "Carrion Thrash");
    }

    @Test
    @DisplayName("Only other creature cards in the controller's graveyard are legal targets")
    void excludesSelfNoncreaturesAndOpponentsCards() {
        DregscapeZombie target = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(new Blightning(), target));
        harness.setGraveyard(player2, List.of(new DregscapeZombie()));
        killInCombat();
        chooseTargetAndResolveUntilPayment(target);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("A legal target must be chosen even if payment will be declined")
    void targetCannotBeDeclined() {
        DregscapeZombie target = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(target));
        killInCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        chooseTargetAndResolveUntilPayment(target);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Accepting payment without enough mana does not return the creature")
    void cannotPayWithOnlyOneMana() {
        DregscapeZombie target = new DregscapeZombie();
        harness.setGraveyard(player1, List.of(target));
        killInCombat();
        chooseTargetAndResolveUntilPayment(target);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Dregscape Zombie");
        harness.assertNotInHand(player1, "Dregscape Zombie");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
    }
}
