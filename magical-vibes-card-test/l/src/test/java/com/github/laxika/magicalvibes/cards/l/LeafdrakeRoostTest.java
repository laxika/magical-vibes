package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.b.BreedingPool;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeafdrakeRoost.class, BreedingPool.class, AssaultZeppelid.class})
class LeafdrakeRoostTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land's ability creates a 2/2 green and blue Drake token with flying")
    void grantedAbilityCreatesDrakeToken() {
        Permanent land = setUpEnchantedLand();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent drake = tokens.getFirst();
        assertThat(drake.getCard().getName()).isEqualTo("Drake");
        assertThat(drake.getCard().hasType(CardType.CREATURE)).isTrue();
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
        Permanent land = setUpEnchantedLand();
        land.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability requires both green and blue mana")
    void grantedAbilityRequiresBothColors() {
        Permanent land = setUpEnchantedLand();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The enchanted land's controller controls the granted ability")
    void enchantedLandControllerControlsAbility() {
        Permanent land = setUpEnchantedLand(player2, player1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .isEmpty();
    }

    @Test
    @DisplayName("Leafdrake Roost can enchant only a land")
    void cannotEnchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        harness.setHand(player1, List.of(new LeafdrakeRoost()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting Leafdrake Roost on an opponent's land grants that opponent the ability")
    void canCastOnOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new BreedingPool());
        harness.setHand(player1, List.of(new LeafdrakeRoost()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(aura.getAttachedTo()).isEqualTo(land.getId());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    @Test
    @DisplayName("Removing the Aura removes the land's granted ability")
    void removingAuraRemovesGrantedAbility() {
        Permanent land = setUpEnchantedLand();
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent != land);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An activated ability still creates its Drake after the Aura leaves")
    void activatedAbilitySurvivesAuraRemoval() {
        Permanent land = setUpEnchantedLand(player2, player1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, 2, null, null);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    private Permanent setUpEnchantedLand() {
        return setUpEnchantedLand(player1, player1);
    }

    private Permanent setUpEnchantedLand(Player landController, Player auraController) {
        Permanent land = harness.addToBattlefieldAndReturn(landController, new BreedingPool());
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new LeafdrakeRoost());
        aura.setAttachedTo(land.getId());
        return land;
    }
}
