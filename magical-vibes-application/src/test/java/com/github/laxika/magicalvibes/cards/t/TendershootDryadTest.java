package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TendershootDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Saproling during each upkeep")
    void createsSaprolingDuringEachUpkeep() {
        harness.addToBattlefield(player1, new TendershootDryad());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        List<Permanent> saprolings = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(saprolings).hasSize(2);
    }

    @Test
    @DisplayName("The city's blessing gives Saprolings +2/+2")
    void blessingBoostsSaprolings() {
        harness.addToBattlefield(player1, new TendershootDryad());
        gd.playersWithCityBlessing.add(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent saproling = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(gqs.getEffectivePower(gd, saproling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, saproling)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gains the city's blessing when it enters as the tenth permanent")
    void gainsBlessingAsTenthPermanent() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new TendershootDryad()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }
}
