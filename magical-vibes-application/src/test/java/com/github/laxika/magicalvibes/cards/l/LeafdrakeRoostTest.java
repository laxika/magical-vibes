package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({LeafdrakeRoost.class, Forest.class, GrizzlyBears.class})
class LeafdrakeRoostTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's ability creates a 2/2 green and blue Drake token with flying")
    void grantedAbilityCreatesDrakeToken() {
        Permanent forest = setUpEnchantedForest();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        Permanent drake = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(drake.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
        assertThat(drake.getCard().getSubtypes()).containsExactly(CardSubtype.DRAKE);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, drake, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A tapped enchanted land cannot activate the granted ability")
    void tappedLandCannotActivate() {
        Permanent forest = setUpEnchantedForest();
        forest.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Leafdrake Roost can enchant only a land")
    void cannotEnchantCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LeafdrakeRoost()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent setUpEnchantedForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LeafdrakeRoost());
        aura.setAttachedTo(forest.getId());
        return forest;
    }
}
