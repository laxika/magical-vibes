package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({NyleasPresence.class, Forest.class, GrizzlyBears.class})
class NyleasPresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Nylea's Presence attaches it to a land and draws a card")
    void resolvingAttachesAndDraws() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new NyleasPresence()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nylea's Presence")
                        && forest.getId().equals(p.getAttachedTo()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Enchanted land gains every basic land type")
    void enchantedLandGainsEveryBasicLandType() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NyleasPresence());
        aura.setAttachedTo(forest.getId());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest))
                .containsExactlyInAnyOrder(
                        CardSubtype.PLAINS,
                        CardSubtype.ISLAND,
                        CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN,
                        CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Enchanted land can tap for each basic color")
    void enchantedLandCanTapForEachBasicColor() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NyleasPresence());
        aura.setAttachedTo(forest.getId());

        List<ManaColor> colors = List.of(
                ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN);
        for (int abilityIndex = 0; abilityIndex < colors.size(); abilityIndex++) {
            harness.activateAbility(player1, 0, abilityIndex, null, null);
            assertThat(gd.playerManaPools.get(player1.getId()).get(colors.get(abilityIndex))).isEqualTo(1);
            forest.untap();
        }
    }

    @Test
    @DisplayName("Nylea's Presence cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NyleasPresence()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Enchanting an opponent's land draws for the Aura controller")
    void canEnchantOpponentsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new NyleasPresence()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactlyInAnyOrder(
                CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                CardSubtype.MOUNTAIN, CardSubtype.FOREST);
        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Added types and mana abilities end when the Aura leaves")
    void auraLeavingEndsGrantedTypesAndAbilities() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new NyleasPresence());
        aura.setAttachedTo(forest.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        forest.untap();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
