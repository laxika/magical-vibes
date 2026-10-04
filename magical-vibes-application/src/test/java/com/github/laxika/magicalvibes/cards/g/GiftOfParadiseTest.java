package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GiftOfParadise.class, Forest.class})
class GiftOfParadiseTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Gift of Paradise attaches to land and controller gains 3 life")
    void resolvingAttachesAndGainsLife() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GiftOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castEnchantment(player1, 0, List.of(forest.getId()));
        // Resolve the aura spell — attaches to land, ETB trigger goes on stack
        harness.passBothPriorities();
        // Resolve the ETB trigger — gain 3 life
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Gift of Paradise")
                        && forest.getId().equals(p.getAttachedTo()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Enchanted land gains mana ability that produces two mana of any one color")
    void enchantedLandGainsManaAbility(ManaColor color) {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiftOfParadise());
        aura.setAttachedTo(forest.getId());

        // Activate the granted mana ability on the forest (ability index 0)
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 2 : 0);
        }
        assertThat(gd.stack).isEmpty();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted land can still produce its normal mana when tapped directly")
    void enchantedLandStillProducesNormalMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiftOfParadise());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Granted mana ability goes away when aura leaves battlefield")
    void manaAbilityRemovedWhenAuraLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiftOfParadise());
        aura.setAttachedTo(forest.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        var staticBonus = gqs.computeStaticBonus(gd, forest);
        assertThat(staticBonus.grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Enchanting an opponent's land gives the Aura controller life and the land controller mana")
    void enchantingOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GiftOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int life1 = gd.getLife(player1.getId());
        int life2 = gd.getLife(player2.getId());

        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Gift of Paradise").getAttachedTo()).isEqualTo(forest.getId());
        harness.assertLife(player1, life1 + 3);
        harness.assertLife(player2, life2);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("No life is gained when the targeted land leaves before the Aura resolves")
    void missingTargetPreventsEnteringAndLifeGain() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GiftOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castEnchantment(player1, 0, forest.getId());
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore);
        harness.assertNotOnBattlefield(player1, "Gift of Paradise");
        harness.assertInGraveyard(player1, "Gift of Paradise");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life-gain trigger resolves even if the Aura leaves after entering")
    void lifeGainSurvivesAuraRemoval() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new GiftOfParadise()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, lifeBefore);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Gift of Paradise"));
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 3);
    }
}
