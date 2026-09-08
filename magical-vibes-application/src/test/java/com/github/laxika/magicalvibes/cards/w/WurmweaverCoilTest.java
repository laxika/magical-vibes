package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({WurmweaverCoil.class, GrizzlyBears.class, AirElemental.class})
class WurmweaverCoilTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Wurmweaver Coil attaches it and gives a green creature +6/+6")
    void resolvingAttachesAndBoostsGreenCreature() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WurmweaverCoil()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(8);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof WurmweaverCoil
                        && permanent.isAttached()
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Wurmweaver Coil does not boost another creature")
    void doesNotBoostAnotherCreature() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        Permanent otherBears = addReadyCreature(player1, new GrizzlyBears());

        Permanent coil = new Permanent(new WurmweaverCoil());
        coil.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(coil);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, otherBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wurmweaver Coil cannot enchant a non-green creature")
    void cannotEnchantNonGreenCreature() {
        Permanent elemental = addReadyCreature(player1, new AirElemental());
        harness.setHand(player1, List.of(new WurmweaverCoil()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, elemental.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a green creature");
    }

    @Test
    @DisplayName("Sacrificing Wurmweaver Coil creates a 6/6 green Wurm token")
    void sacrificingCreatesWurmToken() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        Permanent coil = new Permanent(new WurmweaverCoil());
        coil.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(coil);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WURM);
                    assertThat(token.getEffectivePower()).isEqualTo(6);
                    assertThat(token.getEffectiveToughness()).isEqualTo(6);
                });
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Wurmweaver Coil");
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
