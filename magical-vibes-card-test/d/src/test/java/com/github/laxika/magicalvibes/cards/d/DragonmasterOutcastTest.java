package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonmasterOutcast.class, Forest.class})
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

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        addLands(player1, 6);
        harness.addToBattlefield(player1, new DragonmasterOutcast());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Dragon")).isZero();
    }

    @Test
    @DisplayName("Losing the sixth land before resolution prevents token creation")
    void rechecksLandCountAtResolution() {
        addLands(player1, 6);
        harness.addToBattlefield(player1, new DragonmasterOutcast());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Forest"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gaining the sixth land after upkeep begins does not create a trigger")
    void gainingSixthLandAfterUpkeepBeginsDoesNotTrigger() {
        addLands(player1, 5);
        harness.addToBattlefield(player1, new DragonmasterOutcast());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isZero();
    }

    @Test
    @DisplayName("The trigger still resolves after Dragonmaster Outcast leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        addLands(player1, 6);
        harness.addToBattlefield(player1, new DragonmasterOutcast());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Dragonmaster Outcast"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
        assertThat(countPermanents(player2, "Dragon")).isZero();
    }

    @Test
    @DisplayName("More than six lands still creates exactly one Dragon")
    void createsOneDragonAboveSixLands() {
        addLands(player1, 7);
        harness.addToBattlefield(player1, new DragonmasterOutcast());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates an untapped red Dragon creature token")
    void createsCorrectTokenCharacteristics() {
        addLands(player1, 6);
        harness.addToBattlefield(player1, new DragonmasterOutcast());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(dragon.getCard().isToken()).isTrue();
        assertThat(dragon.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(dragon.isTapped()).isFalse();
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
