package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoundryOfTheConsulsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds one colorless")
    void tapsForColorless() {
        harness.addToBattlefield(player1, new FoundryOfTheConsuls());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifice ability creates two 1/1 flying Thopters")
    void createsTwoThopters() {
        harness.addToBattlefield(player1, new FoundryOfTheConsuls());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Foundry of the Consuls");
        harness.assertInGraveyard(player1, "Foundry of the Consuls");

        List<Permanent> thopters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Thopter"))
                .toList();
        assertThat(thopters).hasSize(2);
        assertThat(thopters).allSatisfy(t -> {
            assertThat(t.getCard().getPower()).isEqualTo(1);
            assertThat(t.getCard().getToughness()).isEqualTo(1);
            assertThat(t.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated without enough mana")
    void requiresFiveMana() {
        harness.addToBattlefield(player1, new FoundryOfTheConsuls());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Foundry of the Consuls");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thopter"));
    }
}
