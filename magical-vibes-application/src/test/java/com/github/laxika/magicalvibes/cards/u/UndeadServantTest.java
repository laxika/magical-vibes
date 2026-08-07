package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UndeadServantTest extends BaseCardTest {

    @Test
    @DisplayName("Creates no tokens when no Undead Servant is in the graveyard")
    void createsNoTokensWithEmptyGraveyard() {
        castServant();

        assertThat(zombieTokens()).isEmpty();
    }

    @Test
    @DisplayName("Creates one 2/2 black Zombie token per Undead Servant in the controller's graveyard")
    void createsOneTokenPerCopyInGraveyard() {
        harness.setGraveyard(player1, List.of(new UndeadServant(), new UndeadServant(), new GrizzlyBears()));

        castServant();

        List<Permanent> tokens = zombieTokens();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(2);
            assertThat(token.getEffectiveToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Counts only the controller's graveyard, not the opponent's")
    void ignoresOpponentGraveyard() {
        harness.setGraveyard(player2, List.of(new UndeadServant(), new UndeadServant()));

        castServant();

        assertThat(zombieTokens()).isEmpty();
    }

    private void castServant() {
        harness.setHand(player1, List.of(new UndeadServant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> zombieTokens() {
        return findPermanents(player1, "Zombie");
    }
}
