package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IcatianTownTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Resolving Icatian Town creates four Citizen tokens")
    void resolvingCreatesFourTokens() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new IcatianTown()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Citizen"))
                .toList();
        assertThat(tokens).hasSize(4);
        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Icatian Town goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new IcatianTown()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Icatian Town"));
    }
}
