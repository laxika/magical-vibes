package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShelteredAerie.class, Forest.class, GrizzlyBears.class})
class ShelteredAerieTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sheltered Aerie attaches it to the target land")
    void resolvingAttachesToTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ShelteredAerie()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ShelteredAerie
                        && forest.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted land gains an ability that produces two mana of one chosen color")
    void enchantedLandGainsManaAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShelteredAerie());
        aura.setAttachedTo(forest.getId());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sheltered Aerie can target only a land")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShelteredAerie()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
