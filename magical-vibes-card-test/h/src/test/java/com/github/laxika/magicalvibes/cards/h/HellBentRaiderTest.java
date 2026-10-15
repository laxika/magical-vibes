package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FloatingShield;
import com.github.laxika.magicalvibes.cards.m.MesmericFiend;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellBentRaider.class, FloatingShield.class, MesmericFiend.class})
class HellBentRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Random discard is paid immediately, before protection resolves")
    void discardIsPaidBeforeResolution() {
        Permanent raider = addCreatureReady(player1, new HellBentRaider());
        FloatingShield discarded = new FloatingShield();
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.WHITE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("A tapped, summoning-sick raider can activate and protects only itself")
    void tappedRaiderProtectsOnlyItself() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new HellBentRaider());
        Permanent other = addCreatureReady(player1, new HellBentRaider());
        Permanent opposing = addCreatureReady(player2, new HellBentRaider());
        raider.tap();
        harness.setHand(player1, List.of(new FloatingShield()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(raider.isTapped()).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, other, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opposing, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("A raider cast this turn can attack with haste")
    void canAttackTheTurnItEnters() {
        harness.castFromHand(player1, new HellBentRaider(), "{1}{R}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("First strike kills a blocker before it deals damage")
    void firstStrikeKillsBlockerBeforeItDealsDamage() {
        Permanent raider = addCreatureReady(player1, new HellBentRaider());
        addCreatureReady(player2, new MesmericFiend());

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackersAndPrepareBlockers(List.of(0));
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            resolveCombat();
            resolveAllTriggers();
        });

        harness.assertOnBattlefield(player1, "Hell-Bent Raider");
        harness.assertInGraveyard(player2, "Mesmeric Fiend");
        assertThat(raider.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Discarding a card at random grants protection from white until end of turn")
    void discardsAndGrantsProtectionFromWhite() {
        Permanent raider = addCreatureReady(player1, new HellBentRaider());
        harness.setHand(player1, List.of(new FloatingShield(), new FloatingShield()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.RED)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Floating Shield");
    }

    @Test
    @DisplayName("The ability cannot be activated without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        Permanent raider = addCreatureReady(player1, new HellBentRaider());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Protection from white prevents a white spell from targeting the raider")
    void protectionStopsWhiteSpell() {
        Permanent raider = addCreatureReady(player1, new HellBentRaider());
        harness.setHand(player1, List.of(new FloatingShield()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new FloatingShield()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, raider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Protection from white wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent raider = addCreatureReady(player1, new HellBentRaider());
        harness.setHand(player1, List.of(new FloatingShield()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, raider, CardColor.WHITE)).isFalse();
    }
}
