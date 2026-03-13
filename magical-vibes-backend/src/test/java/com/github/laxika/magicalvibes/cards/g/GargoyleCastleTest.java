package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GargoyleCastleTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Gargoyle Castle has correct card properties")
    void hasCorrectProperties() {
        GargoyleCastle card = new GargoyleCastle();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        var manaAbility = card.getActivatedAbilities().get(0);
        assertThat(manaAbility.isRequiresTap()).isTrue();
        assertThat(manaAbility.getManaCost()).isNull();
        assertThat(manaAbility.isNeedsTarget()).isFalse();
        assertThat(manaAbility.getEffects()).hasSize(1);
        assertThat(manaAbility.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);

        var sacrificeAbility = card.getActivatedAbilities().get(1);
        assertThat(sacrificeAbility.isRequiresTap()).isTrue();
        assertThat(sacrificeAbility.getManaCost()).isEqualTo("{5}");
        assertThat(sacrificeAbility.isNeedsTarget()).isFalse();
        assertThat(sacrificeAbility.getEffects()).hasSize(2);
        assertThat(sacrificeAbility.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(sacrificeAbility.getEffects().get(1)).isInstanceOf(CreateCreatureTokenEffect.class);

        CreateCreatureTokenEffect tokenEffect = (CreateCreatureTokenEffect) sacrificeAbility.getEffects().get(1);
        assertThat(tokenEffect.tokenName()).isEqualTo("Gargoyle");
        assertThat(tokenEffect.power()).isEqualTo(3);
        assertThat(tokenEffect.toughness()).isEqualTo(4);
        assertThat(tokenEffect.color()).isNull();
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.GARGOYLE);
        assertThat(tokenEffect.keywords()).containsExactly(Keyword.FLYING);
        assertThat(tokenEffect.additionalTypes()).containsExactly(CardType.ARTIFACT);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new GargoyleCastle());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Sacrifice ability: activation =====

    @Test
    @DisplayName("Activating sacrifice ability puts token creation on the stack")
    void activatingPutsOnStack() {
        harness.addToBattlefield(player1, new GargoyleCastle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gargoyle Castle");
    }

    @Test
    @DisplayName("Gargoyle Castle is sacrificed as a cost before resolution")
    void sacrificedBeforeResolution() {
        harness.addToBattlefield(player1, new GargoyleCastle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        // Before resolution, Gargoyle Castle should already be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Gargoyle Castle"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gargoyle Castle"));
    }

    @Test
    @DisplayName("Mana is consumed when activating sacrifice ability")
    void manaIsConsumedWhenActivating() {
        harness.addToBattlefield(player1, new GargoyleCastle());
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Sacrifice ability: resolution =====

    @Test
    @DisplayName("Resolving ability creates a 3/4 Gargoyle token")
    void resolvingCreatesGargoyleToken() {
        harness.addToBattlefield(player1, new GargoyleCastle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gargoyle"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GARGOYLE);
    }

    @Test
    @DisplayName("Gargoyle token has flying")
    void gargoyleTokenHasFlying() {
        harness.addToBattlefield(player1, new GargoyleCastle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gargoyle"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gargoyle token is an artifact creature")
    void gargoyleTokenIsArtifactCreature() {
        harness.addToBattlefield(player1, new GargoyleCastle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gargoyle"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.isCreature(gd, token)).isTrue();
    }

    @Test
    @DisplayName("Gargoyle token enters with summoning sickness")
    void tokenEntersWithSummoningSickness() {
        harness.addToBattlefield(player1, new GargoyleCastle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gargoyle"))
                .findFirst().orElseThrow();
        assertThat(token.isSummoningSick()).isTrue();
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate sacrifice ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new GargoyleCastle());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate sacrifice ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new GargoyleCastle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        // Tap for mana first
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Game log =====

    @Test
    @DisplayName("Creating Gargoyle token is logged")
    void tokenCreationIsLogged() {
        harness.addToBattlefield(player1, new GargoyleCastle());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Gargoyle") && log.contains("token"));
    }
}
