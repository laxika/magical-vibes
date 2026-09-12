package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OtawaraSoaringCity.class, Forest.class, GrizzlyBears.class, Spellbook.class})
class OtawaraSoaringCityTest extends BaseCardTest {

    @Test
    @DisplayName("Adds blue mana")
    void addsBlueMana() {
        harness.addToBattlefield(player1, new OtawaraSoaringCity());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel returns an artifact and is reduced by each legendary creature")
    void channelReturnsArtifactWithLegendaryCostReduction() {
        GrizzlyBears legendaryCreature = new GrizzlyBears();
        legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        harness.addToBattlefield(player1, legendaryCreature);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player2, "Spellbook");
        harness.assertInGraveyard(player1, "Otawara, Soaring City");
    }

    @Test
    @DisplayName("Channel cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new OtawaraSoaringCity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact, creature, enchantment, or planeswalker");
    }
}
