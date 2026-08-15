package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DragonmasterOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 5/5 flying Dragon at six lands")
    void createsDragonAtSixLands() {
        addLands(player1, 6);
        harness.addToBattlefield(player1, new DragonmasterOutcast());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not create a Dragon below six lands")
    void doesNotCreateDragonBelowSixLands() {
        addLands(player1, 5);
        harness.addToBattlefield(player1, new DragonmasterOutcast());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isZero();
    }

    @Test
    @DisplayName("Only the Outcast's controller's lands count")
    void onlyControllerLandsCount() {
        addLands(player2, 6);
        harness.addToBattlefield(player1, new DragonmasterOutcast());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isZero();
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
