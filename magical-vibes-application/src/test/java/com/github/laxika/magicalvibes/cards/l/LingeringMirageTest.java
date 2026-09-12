package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.Acridian;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LingeringMirage.class, Forest.class, Acridian.class})
class LingeringMirageTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes an Island and produces blue mana")
    void enchantedLandBecomesIsland() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new LingeringMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Enchanted land has only the Island basic land type")
    void enchantedLandSubtypeIsOnlyIsland() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LingeringMirage());
        aura.setAttachedTo(forest.getId());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Lingering Mirage can enchant an opponent's land")
    void canEnchantOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LingeringMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Land resumes normal mana production when Lingering Mirage leaves")
    void landResumesNormalManaProductionWhenAuraLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LingeringMirage());
        aura.setAttachedTo(forest.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cycling discards Lingering Mirage and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new LingeringMirage()));
        harness.setLibrary(player1, List.of(new Acridian()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lingering Mirage");
        harness.assertInHand(player1, "Acridian");
    }

    @Test
    @DisplayName("Cannot cast Lingering Mirage targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Acridian());
        Permanent acridian = findPermanent(player1, "Acridian");
        harness.setHand(player1, List.of(new LingeringMirage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, acridian.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
