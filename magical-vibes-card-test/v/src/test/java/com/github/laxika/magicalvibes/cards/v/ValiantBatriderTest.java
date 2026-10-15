package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ValiantBatrider.class, DarkRitual.class, GrizzlyBears.class})
class ValiantBatriderTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged player may pay {1} to prevent the boon from making opponents draw")
    void damagedPlayerMayPayToPreventOpponentsDrawing() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        dealCombatDamageToPlayer2();

        castDarkRitualWithMana(3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the payment makes each opponent draw and consumes the boon")
    void decliningPaymentMakesOpponentsDrawOnce() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        dealCombatDamageToPlayer2();

        castDarkRitualWithMana(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.castFromHand(player2, new DarkRitual(), "{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The boon ignores creature spells and waits for a noncreature spell")
    void creatureSpellDoesNotConsumeBoon() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        dealCombatDamageToPlayer2();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        castDarkRitualWithMana(1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Paying consumes the boon")
    void payingConsumesBoon() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        dealCombatDamageToPlayer2();

        castDarkRitualWithMana(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player2, new DarkRitual(), "{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The boon still triggers after Valiant Batrider leaves the battlefield")
    void boonSurvivesSourceLeavingBattlefield() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        dealCombatDamageToPlayer2();
        Permanent batrider = findPermanent(player1, "Valiant Batrider");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, batrider));

        castDarkRitualWithMana(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Two combat damage events give separate boons that trigger on the same spell")
    void multipleBoonsTriggerSeparately() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of());
        Permanent secondBatrider = addCreatureReady(player1, new ValiantBatrider());
        secondBatrider.setAttacking(true);
        dealCombatDamageToPlayer2();

        castDarkRitualWithMana(3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        harness.castFromHand(player2, new DarkRitual(), "{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void dealCombatDamageToPlayer2() {
        Permanent batrider = addCreatureReady(player1, new ValiantBatrider());
        batrider.setAttacking(true);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            resolveCombat();
            resolveAllTriggers();
        });

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castDarkRitualWithMana(int amount) {
        harness.addMana(player2, ManaColor.BLACK, amount - 1);
        harness.castFromHand(player2, new DarkRitual(), "{B}");
    }
}
