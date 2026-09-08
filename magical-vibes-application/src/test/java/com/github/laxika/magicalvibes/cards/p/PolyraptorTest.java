package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PolyraptorTest extends BaseCardTest {

    private long polyraptorCount() {
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Polyraptor"))
                .count();
    }

    @Test
    @DisplayName("When dealt damage, creates a 5/5 token copy")
    void damageCreatesTokenCopy() {
        harness.addToBattlefield(player2, new Polyraptor());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID polyraptorId = harness.getPermanentId(player2, "Polyraptor");
        harness.castInstant(player1, 0, polyraptorId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(polyraptorCount()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Polyraptor"))
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(5);
                    assertThat(token.getCard().getToughness()).isEqualTo(5);
                });
    }

    @Test
    @DisplayName("Token copies retain the damage trigger")
    void tokenCopyRetainsDamageTrigger() {
        harness.addToBattlefield(player2, new Polyraptor());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID polyraptorId = harness.getPermanentId(player2, "Polyraptor");
        harness.castInstant(player1, 0, polyraptorId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        UUID tokenId = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Polyraptor"))
                .findFirst()
                .orElseThrow()
                .getId();
        harness.castInstant(player1, 0, tokenId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(polyraptorCount()).isEqualTo(3);
    }
}
