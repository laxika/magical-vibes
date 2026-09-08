package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WardenOfTheWoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger from an opponent's spell draws two cards")
    void acceptingOpponentSpellTriggerDrawsTwoCards() {
        Permanent warden = addWarden(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, warden.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Declining the trigger does not draw cards")
    void decliningOpponentSpellTriggerDoesNotDraw() {
        Permanent warden = addWarden(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, warden.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("An opponent's ability targeting it also triggers the draw")
    void opponentAbilityTriggerDrawsTwoCards() {
        Permanent warden = addWarden(player1);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player2, 0, null, warden.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("A spell controlled by its controller does not trigger it")
    void ownSpellDoesNotTrigger() {
        Permanent warden = addWarden(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, warden.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    private Permanent addWarden(Player player) {
        return harness.addToBattlefieldAndReturn(player, new WardenOfTheWoods());
    }
}
