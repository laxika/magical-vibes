package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToCardsInHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SturmgeistTest extends BaseCardTest {

    @Test
    @DisplayName("Sturmgeist has correct effects registered")
    void hasCorrectEffects() {
        Sturmgeist card = new Sturmgeist();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PowerToughnessEqualToCardsInHandEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(DrawCardEffect.class);
    }

    @Test
    @DisplayName("P/T equals number of cards in controller's hand")
    void ptEqualsHandSize() {
        Permanent sturmgeist = addSturmgeistReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(3);
    }

    @Test
    @DisplayName("P/T updates dynamically as hand size changes")
    void ptUpdatesDynamically() {
        Permanent sturmgeist = addSturmgeistReady(player1);
        gd.playerHands.get(player1.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(1);

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(2);

        gd.playerHands.get(player1.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(0);
    }

    @Test
    @DisplayName("P/T counts only controller's hand, not opponent's")
    void countsOnlyControllerHand() {
        Permanent sturmgeist = addSturmgeistReady(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();

        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player2.getId()).add(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card when dealing combat damage to a player")
    void drawsCardOnCombatDamage() {
        Permanent sturmgeist = addSturmgeistReady(player1);
        sturmgeist.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Resolve the combat damage trigger
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Drawing from combat damage increases P/T")
    void combatDamageDrawIncreasesPT() {
        Permanent sturmgeist = addSturmgeistReady(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        sturmgeist.setAttacking(true);
        harness.setLife(player2, 20);

        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(2);

        resolveCombat();

        // Resolve the combat damage trigger (draws a card)
        harness.passBothPriorities();

        // Hand now has 3 cards (2 original + 1 drawn)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, sturmgeist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sturmgeist)).isEqualTo(3);
    }

    @Test
    @DisplayName("No trigger when Sturmgeist is blocked and killed")
    void noTriggerWhenBlocked() {
        Permanent sturmgeist = addSturmgeistReady(player1);
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        sturmgeist.setAttacking(true);

        // Serra Angel (4/4 flying) blocks and kills the 2/2 Sturmgeist
        Permanent blocker = new Permanent(new SerraAngel());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sturmgeist"));
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addSturmgeistReady(Player player) {
        Sturmgeist card = new Sturmgeist();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
