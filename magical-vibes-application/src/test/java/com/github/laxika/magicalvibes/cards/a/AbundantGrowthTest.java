package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbundantGrowth.class, Forest.class})
class AbundantGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Abundant Growth attaches to land and controller draws a card")
    void resolvingAttachesAndDraws() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AbundantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castEnchantment(player1, 0, List.of(forest.getId()));
        // Resolve the aura spell — attaches to land, ETB trigger goes on stack
        harness.passBothPriorities();
        // Resolve the ETB trigger — draw a card
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Abundant Growth")
                        && forest.getId().equals(p.getAttachedTo()));
        // Hand emptied by casting, then drew 1 from library
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 1);
    }

    @Test
    @DisplayName("Enchanted land gains mana ability that produces one mana of any color")
    void enchantedLandGainsManaAbility() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AbundantGrowth());
        aura.setAttachedTo(forest.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted land can still produce its normal mana when tapped directly")
    void enchantedLandStillProducesNormalMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AbundantGrowth());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void grantedAbilityProducesExactlyOneManaWithoutUsingTheStack(ManaColor color) {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AbundantGrowth());
        aura.setAttachedTo(forest.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.stack).isEmpty();
        for (ManaColor candidate : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(candidate))
                    .isEqualTo(candidate == color ? 1 : 0);
        }
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void opponentControlsGrantedManaAbilityButAuraControllerDraws() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AbundantGrowth()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, List.of(forest.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void targetLeavingBeforeResolutionPreventsEntryAndDraw() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AbundantGrowth()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, List.of(forest.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerGraveyards.get(player1.getId()).add(forest.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Abundant Growth");
        harness.assertInGraveyard(player1, "Abundant Growth");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Granted mana ability goes away when aura leaves battlefield")
    void manaAbilityRemovedWhenAuraLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AbundantGrowth());
        aura.setAttachedTo(forest.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        var staticBonus = gqs.computeStaticBonus(gd, forest);
        assertThat(staticBonus.grantedActivatedAbilities()).isEmpty();
    }
}
