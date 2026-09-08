package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpontaneousGenerationTest extends BaseCardTest {

    private long saprolings(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Saproling"))
                .count();
    }

    @Test
    @DisplayName("Creates one Saproling for each card in hand")
    void createsOneSaprolingPerCardInHand() {
        harness.setHand(player1, List.of(
                new SpontaneousGeneration(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(saprolings(player1)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creates no Saprolings when no cards remain in hand")
    void createsNoSaprolingsWithEmptyHand() {
        harness.setHand(player1, List.of(new SpontaneousGeneration()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(saprolings(player1)).isZero();
    }

    @Test
    @DisplayName("Saprolings are 1/1 green creatures")
    void createsOneOneGreenSaprolings() {
        harness.setHand(player1, List.of(new SpontaneousGeneration(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Saproling"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.GREEN);
    }
}
