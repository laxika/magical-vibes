package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleshlessGladiatorTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard tapped and you lose 1 life with a corrupted opponent")
    void returnsTappedAndLosesLife() {
        FleshlessGladiator gladiator = new FleshlessGladiator();
        harness.setGraveyard(player1, List.of(gladiator));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(gladiator.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(gladiator.getId()));
    }

    @Test
    @DisplayName("Requires an opponent to have three poison counters")
    void requiresOpponentPoisonThreshold() {
        FleshlessGladiator gladiator = new FleshlessGladiator();
        harness.setGraveyard(player1, List.of(gladiator));
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gladiator);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(gladiator.getId()));
    }
}
