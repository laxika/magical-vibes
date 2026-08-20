package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonbackAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it deals 3 damage to each creature and planeswalker, but not players")
    void etbDamagesCreaturesAndPlaneswalkersNotPlayers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castDragonbackAssault();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Landfall creates a 4/4 red Dragon token with flying")
    void landfallCreatesDragonToken() {
        castDragonbackAssault();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent dragon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Dragon"))
                .findFirst()
                .orElseThrow();
        assertThat(dragon.getEffectivePower()).isEqualTo(4);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(4);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("An opponent's landfall does not create a Dragon token")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new DragonbackAssault());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Dragon")))
                .isEmpty();
    }

    private void castDragonbackAssault() {
        harness.setHand(player1, List.of(new DragonbackAssault()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
