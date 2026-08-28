package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllTimedExplosion.class, GrizzlyBears.class, WindDrake.class, HillGiant.class})
class IllTimedExplosionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two, optionally discards two, and damages each creature by the greatest discarded mana value")
    void damagesByGreatestDiscardedManaValue() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new IllTimedExplosion()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new WindDrake()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);

        int windDrakeIndex = gd.playerHands.get(player1.getId()).indexOf(
                gd.playerHands.get(player1.getId()).stream()
                        .filter(card -> card instanceof WindDrake)
                        .findFirst().orElseThrow());
        harness.handleCardChosen(player1, windDrakeIndex);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the discard still keeps the drawn cards and deals no damage")
    void decliningDiscardDealsNoDamage() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new IllTimedExplosion()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new WindDrake()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
