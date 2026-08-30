package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeemingDragonstorm.class, GrizzlyBears.class, ShivanDragon.class})
class TeemingDragonstormTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates two 2/2 white Soldier tokens")
    void enteringCreatesSoldierTokens() {
        harness.setHand(player1, List.of(new TeemingDragonstorm()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> soldiers = battlefield.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .toList();

        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getPower()).isEqualTo(2);
            assertThat(soldier.getCard().getToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Returns to its owner's hand when a Dragon you control enters")
    void returnsWhenAllyDragonEnters() {
        harness.addToBattlefield(player1, new TeemingDragonstorm());
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Teeming Dragonstorm");
        harness.assertInHand(player1, "Teeming Dragonstorm");
    }

    @Test
    @DisplayName("Does not return when a non-Dragon creature enters")
    void doesNotReturnForNonDragon() {
        harness.addToBattlefield(player1, new TeemingDragonstorm());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Teeming Dragonstorm");
    }

    @Test
    @DisplayName("Does not return when an opponent's Dragon enters")
    void doesNotReturnForOpponentDragon() {
        harness.addToBattlefield(player1, new TeemingDragonstorm());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ShivanDragon()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Teeming Dragonstorm");
    }
}
