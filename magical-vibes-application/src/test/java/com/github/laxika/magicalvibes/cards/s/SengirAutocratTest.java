package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed(SengirAutocrat.class)
class SengirAutocratTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates three 0/1 black Serf tokens")
    void etbCreatesThreeSerfTokens() {
        castAndResolveAutocrat(player1);

        List<Permanent> serfs = serfTokens(player1);
        assertThat(serfs).hasSize(3);
        Permanent serf = serfs.getFirst();
        assertThat(serf.getCard().isToken()).isTrue();
        assertThat(serf.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(serf.getCard().getSubtypes()).containsExactly(CardSubtype.SERF);
        assertThat(serf.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(serf.getEffectivePower()).isEqualTo(0);
        assertThat(serf.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Leaving the battlefield exiles all Serf tokens")
    void leavesBattlefieldExilesSerfTokens() {
        Permanent autocrat = castAndResolveAutocrat(player1);

        assertThat(serfTokens(player1)).hasSize(3);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, autocrat));
        resolveAllTriggers();

        assertThat(serfTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Leaving the battlefield exiles Serf tokens controlled by both players")
    void leavesBattlefieldExilesAllPlayersSerfTokens() {
        Permanent autocrat = castAndResolveAutocrat(player1);
        harness.enterBattlefieldAndReturn(player2, new SengirAutocrat());
        resolveAllTriggers();

        assertThat(serfTokens(player1)).hasSize(3);
        assertThat(serfTokens(player2)).hasSize(3);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, autocrat));
        resolveAllTriggers();

        assertThat(serfTokens(player1)).isEmpty();
        assertThat(serfTokens(player2)).isEmpty();
        assertThat(findPermanents(player2, "Sengir Autocrat")).hasSize(1);
    }

    private Permanent castAndResolveAutocrat(com.github.laxika.magicalvibes.model.Player player) {
        harness.castFromHand(player, new SengirAutocrat(), "{3}{B}");
        resolveAllTriggers();
        return findPermanent(player, "Sengir Autocrat");
    }

    private List<Permanent> serfTokens(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanents(player, "Serf");
    }
}
