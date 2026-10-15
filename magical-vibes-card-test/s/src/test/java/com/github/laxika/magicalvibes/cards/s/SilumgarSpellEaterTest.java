package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilumgarSpellEater.class, Shock.class})
class SilumgarSpellEaterTest extends BaseCardTest {
    @Test
    void payingMegamorphCostAddsCounterEvenWithoutASpellToTarget() {
        Permanent spellEater = castFaceDown();

        turnFaceUp(spellEater);

        assertThat(spellEater.isFaceDown()).isFalse();
        assertThat(spellEater.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void megamorphCounterIsPresentBeforeCounterTriggerResolves() {
        Permanent spellEater = castFaceDown();
        Shock shock = castShock(1);

        turnFaceUp(spellEater);

        assertThat(spellEater.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(shock.getId());
    }

    @Test
    void turningFaceUpCountersTargetSpellWhenItsControllerDeclinesPayment() {
        Permanent spellEater = castFaceDown();
        Shock shock = castShock(4);

        turnFaceUp(spellEater);

        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFaceUpDoesNotAddMegamorphCounterOrTriggerCounterAbility() {
        harness.castFromHand(player1, new SilumgarSpellEater(), "{2}{U}");
        harness.passBothPriorities();

        Permanent spellEater = findPermanent(player1, "Silumgar Spell-Eater");
        assertThat(spellEater.isFaceDown()).isFalse();
        assertThat(spellEater.getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpCountersTargetSpellWhenItsControllerCannotPay() {
        Permanent spellEater = castFaceDown();
        Shock shock = castShock(1);

        turnFaceUp(spellEater);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(shock.getId());
        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void turningFaceUpLeavesTargetSpellOnTheStackWhenItsControllerPays() {
        Permanent spellEater = castFaceDown();
        Shock shock = castShock(4);

        turnFaceUp(spellEater);

        harness.handlePermanentChosen(player1, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Shock");
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new SilumgarSpellEater()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Silumgar Spell-Eater");
    }

    private Shock castShock(int mana) {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, mana);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        return shock;
    }

    private void turnFaceUp(Permanent spellEater) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(spellEater));
    }
}
