package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhipSilkTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has reach")
    void enchantedCreatureHasReach() {
        Permanent bears = attachWhipSilk();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Creature loses reach when Whip Silk leaves the battlefield")
    void reachLostWhenAuraLeaves() {
        Permanent bears = attachWhipSilk();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Whip Silk"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Activated ability returns Whip Silk to its owner's hand")
    void activatedAbilityReturnsSelfToHand() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new WhipSilk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        int auraIndex = -1;
        var battlefield = gd.playerBattlefields.get(player1.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals("Whip Silk")) {
                auraIndex = i;
                break;
            }
        }
        assertThat(auraIndex).isGreaterThanOrEqualTo(0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Whip Silk");
        harness.assertInHand(player1, "Whip Silk");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new WhipSilk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    private Permanent attachWhipSilk() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        Permanent aura = new Permanent(new WhipSilk());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        return bears;
    }
}
