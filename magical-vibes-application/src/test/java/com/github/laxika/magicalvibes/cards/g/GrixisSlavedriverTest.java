package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Grixis Slavedriver")
class GrixisSlavedriverTest extends BaseCardTest {

    // ===== When this creature leaves the battlefield, create a 2/2 black Zombie token =====

    @Test
    @DisplayName("Leaving the battlefield (bounce) creates a 2/2 black Zombie token")
    void leavingBattlefieldCreatesZombieToken() {
        harness.addToBattlefield(player1, new GrixisSlavedriver());
        Permanent slavedriver = findPermanent(player1, "Grixis Slavedriver");

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, slavedriver.getId());
        harness.passBothPriorities(); // Unsummon resolves → Slavedriver returns to hand, trigger onto stack
        harness.passBothPriorities(); // leaves-battlefield trigger resolves → token created

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getCard().getPower()).isEqualTo(2);
        assertThat(tokens.get(0).getCard().getToughness()).isEqualTo(2);
        assertThat(tokens.get(0).getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(tokens.get(0).getCard().isToken()).isTrue();
    }

    // ===== Unearth {3}{B} =====

    @Test
    @DisplayName("Unearth returns Grixis Slavedriver to the battlefield with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new GrixisSlavedriver()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Grixis Slavedriver");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grixis Slavedriver"));
    }

    @Test
    @DisplayName("Unearth-exile at the next end step still triggers the leaves-battlefield token")
    void unearthExileAtEndStepCreatesToken() {
        harness.setGraveyard(player1, List.of(new GrixisSlavedriver()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step → Slavedriver exiled, leaves-battlefield trigger onto stack
        harness.passBothPriorities(); // trigger resolves → token created

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grixis Slavedriver"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grixis Slavedriver"));

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(tokens).hasSize(1);
    }
}
