package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TomakulHonorGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an opponent's spell when its controller cannot pay {2}")
    void countersOpponentSpellWithoutPayment() {
        Permanent honorGuard = addReadyHonorGuard();
        Shock shock = new Shock();
        prepareOpponentCast(List.of(shock), ManaColor.RED, 1);

        harness.castInstant(player2, 0, honorGuard.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(honorGuard.getId()));
    }

    @Test
    @DisplayName("Allows an opponent's spell after its controller pays {2}")
    void allowsOpponentSpellAfterPayment() {
        Permanent honorGuard = addReadyHonorGuard();
        Unsummon unsummon = new Unsummon();
        prepareOpponentCast(List.of(unsummon), ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, honorGuard.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(honorGuard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(honorGuard.getCard().getId()));
    }

    @Test
    @DisplayName("Triggers for an opponent's targeted ability")
    void countersOpponentAbilityWithoutPayment() {
        Permanent honorGuard = addReadyHonorGuard();
        Permanent sorcerer = new Permanent(new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(sorcerer);
        forceOpponentMainPhase();

        harness.activateAbility(player2, 0, null, honorGuard.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(honorGuard.getId()));
    }

    @Test
    @DisplayName("Does not trigger for a spell controlled by its controller")
    void doesNotTriggerForControllerSpell() {
        Permanent honorGuard = addReadyHonorGuard();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, honorGuard.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyHonorGuard() {
        Permanent honorGuard = new Permanent(new TomakulHonorGuard());
        honorGuard.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(honorGuard);
        return honorGuard;
    }

    private void prepareOpponentCast(List<Card> hand,
                                     ManaColor color, int mana) {
        forceOpponentMainPhase();
        harness.setHand(player2, hand);
        harness.addMana(player2, color, mana);
    }

    private void forceOpponentMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
