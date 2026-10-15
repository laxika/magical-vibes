package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CatacombSlug;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndeadServant.class, CatacombSlug.class})
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
        harness.setGraveyard(player1, List.of(new UndeadServant(), new UndeadServant(), new CatacombSlug()));

        castServant();

        List<Permanent> tokens = zombieTokens();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
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

    @Test
    @DisplayName("Counts copies added to the graveyard after the ability triggers")
    void countsCopiesAtResolution() {
        harness.castFromHand(player1, new UndeadServant(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(zombieTokens()).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new UndeadServant(), new UndeadServant()));
        harness.passBothPriorities();

        assertThat(zombieTokens()).hasSize(2);
    }

    @Test
    @DisplayName("Creates no tokens if the last matching graveyard card leaves before resolution")
    void ignoresCopiesRemovedBeforeResolution() {
        harness.setGraveyard(player1, List.of(new UndeadServant()));
        harness.castFromHand(player1, new UndeadServant(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new CatacombSlug()));
        harness.passBothPriorities();

        assertThat(zombieTokens()).isEmpty();
    }

    private void castServant() {
        harness.castFromHand(player1, new UndeadServant(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> zombieTokens() {
        return findPermanents(player1, "Zombie");
    }
}
