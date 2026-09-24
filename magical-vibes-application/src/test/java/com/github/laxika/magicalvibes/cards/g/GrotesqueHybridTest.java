package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SpiritFlare;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GrotesqueHybrid.class, Gurzigost.class, SpiritFlare.class})
class GrotesqueHybridTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage destroys the damaged creature despite a regeneration shield")
    void combatDamageDestroysWithoutRegeneration() {
        addCreatureReady(player1, new GrotesqueHybrid());
        Permanent gurzigost = addCreatureReady(player2, new Gurzigost());
        gurzigost.setRegenerationShield(1);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(gurzigost);
    }

    @Test
    @DisplayName("The trigger does not fire for noncombat damage")
    void noncombatDamageDoesNotDestroy() {
        Permanent hybrid = addCreatureReady(player1, new GrotesqueHybrid());
        Permanent gurzigost = addCreatureReady(player2, new Gurzigost());
        gurzigost.setAttacking(true);
        harness.setHand(player1, List.of(new SpiritFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, List.of(hybrid.getId(), gurzigost.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gurzigost.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(gurzigost);
    }

    @Test
    @DisplayName("Discarding a card grants flying and protection from green and white until end of turn")
    void discardGrantsFlyingAndProtection() {
        Permanent hybrid = addCreatureReady(player1, new GrotesqueHybrid());
        Card discarded = new SpiritFlare();
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(hybrid.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(hybrid.getProtectionFromColorsUntilEndOfTurn())
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(hybrid.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(hybrid.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }
}
