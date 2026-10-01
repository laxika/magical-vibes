package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Demonfire;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
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

@CardUsed({HaazdaShieldMate.class, Demonfire.class, MistralCharger.class})
class HaazdaShieldMateTest extends BaseCardTest {

    @Test
    @DisplayName("Declining its upkeep payment sacrifices Haazda Shield Mate")
    void decliningUpkeepPaymentSacrificesIt() {
        Permanent shieldMate = harness.addToBattlefieldAndReturn(player1, new HaazdaShieldMate());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shieldMate);
        harness.assertInGraveyard(player1, "Haazda Shield Mate");
    }

    @Test
    @DisplayName("Paying its upkeep cost keeps Haazda Shield Mate on the battlefield")
    void payingUpkeepPaymentKeepsIt() {
        Permanent shieldMate = harness.addToBattlefieldAndReturn(player1, new HaazdaShieldMate());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shieldMate);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source")
    void preventsNextDamageFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyShieldMate(player1);
        Permanent source = addReadyMistralCharger(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from a different source is not prevented")
    void damageFromDifferentSourceIsNotPrevented() {
        harness.setLife(player1, 20);
        addReadyShieldMate(player1);
        Permanent chosenSource = addReadyMistralCharger(player2);
        Permanent otherSource = addReadyMistralCharger(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenSource.getId());

        otherSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.sourceId().equals(chosenSource.getId()));
    }

    @Test
    @DisplayName("Prevents damage from a chosen spell on the stack")
    void preventsDamageFromChosenSpellOnStack() {
        harness.setLife(player1, 20);
        addReadyShieldMate(player1);
        Demonfire demonfire = new Demonfire();
        harness.setHand(player2, List.of(demonfire, new MistralCharger()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, 1, player1.getId());
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(demonfire.getId());

        harness.handlePermanentChosen(player1, demonfire.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Demonfire");
    }

    private Permanent addReadyShieldMate(Player player) {
        return addCreatureReady(player, new HaazdaShieldMate());
    }

    private Permanent addReadyMistralCharger(Player player) {
        return addCreatureReady(player, new MistralCharger());
    }
}
