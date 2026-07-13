package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SengirAutocratTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates three 0/1 black Serf tokens")
    void etbCreatesThreeSerfTokens() {
        harness.setHand(player1, List.of(new SengirAutocrat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> serfs = serfTokens(player1);
        assertThat(serfs).hasSize(3);
        Permanent serf = serfs.getFirst();
        assertThat(serf.getCard().isToken()).isTrue();
        assertThat(serf.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(serf.getEffectivePower()).isEqualTo(0);
        assertThat(serf.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Leaving the battlefield exiles all Serf tokens")
    void leavesBattlefieldExilesSerfTokens() {
        harness.setHand(player1, List.of(new SengirAutocrat()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger -> three Serf tokens

        assertThat(serfTokens(player1)).hasSize(3);

        Permanent autocrat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sengir Autocrat"))
                .findFirst()
                .orElseThrow();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, autocrat);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // resolve LTB trigger -> exile Serfs

        assertThat(serfTokens(player1)).isEmpty();
    }

    private List<Permanent> serfTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Serf"))
                .toList();
    }
}
