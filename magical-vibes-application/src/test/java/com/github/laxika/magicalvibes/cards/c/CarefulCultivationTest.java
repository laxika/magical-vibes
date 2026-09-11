package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarefulCultivation.class, Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class CarefulCultivationTest extends BaseCardTest {

    @Test
    @DisplayName("Careful Cultivation boosts an enchanted creature and grants reach and mana")
    void boostsCreatureAndGrantsAbilities() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.setHand(player1, List.of(new CarefulCultivation()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Careful Cultivation can enchant an artifact without creature bonuses")
    void canEnchantArtifactWithoutCreatureBonuses() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CarefulCultivation()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gqs.hasKeyword(gd, artifact, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Careful Cultivation cannot enchant a land")
    void cannotEnchantLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CarefulCultivation()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Channel creates a Human Monk with a green mana ability")
    void channelCreatesHumanMonk() {
        harness.setHand(player1, List.of(new CarefulCultivation()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Careful Cultivation");
        Permanent monk = findPermanent(player1, "Human Monk");
        assertThat(monk.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(monk.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.MONK);
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);

        monk.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(monk), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(monk.isTapped()).isTrue();
    }
}
