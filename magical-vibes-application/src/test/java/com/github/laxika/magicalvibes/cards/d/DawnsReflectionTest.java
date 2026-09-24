package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnsReflection.class, Forest.class})
class DawnsReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's controller chooses each of the two additional mana colors")
    void addsTwoManaInAnyCombinationOfColors() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new DawnsReflection()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);
        harness.handleListChoice(player1, ManaColor.WHITE.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the enchanted land gets the additional mana")
    void onlyEnchantedLandGetsBonus() {
        Permanent enchantedForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent otherForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DawnsReflection());
        aura.setAttachedTo(enchantedForest.getId());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(otherForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted land's controller gets both additional mana of the chosen color")
    void givesAdditionalManaToEnchantedLandsController() {
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new DawnsReflection()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, opponentForest.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player2, 0);
        harness.handleListChoice(player2, ManaColor.RED.name());
        harness.handleListChoice(player2, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
