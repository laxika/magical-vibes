package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DawnsReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's controller chooses each of the two additional mana colors")
    void addsTwoManaInAnyCombinationOfColors() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
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
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent enchantedForest = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent otherForest = gd.playerBattlefields.get(player1.getId()).get(1);
        Permanent aura = new Permanent(new DawnsReflection());
        aura.setAttachedTo(enchantedForest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(otherForest.isTapped()).isTrue();
    }
}
