package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TarmogoyfNest.class, Forest.class, GrizzlyBears.class, Shock.class})
class TarmogoyfNestTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land creates a dynamic Tarmogoyf token")
    void enchantedLandCreatesDynamicTarmogoyfToken() {
        Permanent forest = setUpEnchantedForest();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        Permanent token = findPermanent(player1, "Tarmogoyf");
        assertThat(token.getCard().getName()).isEqualTo("Tarmogoyf");
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.LHURGOYF);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    @DisplayName("Tarmogoyf Nest can enchant only a land")
    void cannotEnchantNonlandPermanent() {
        Permanent nest = harness.addToBattlefieldAndReturn(player1, new TarmogoyfNest());
        harness.setHand(player1, List.of(new TarmogoyfNest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Created Tarmogoyf has mana value two")
    void createdTarmogoyfHasManaValueTwo() {
        setUpEnchantedForest();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Tarmogoyf");
        assertThat(token.getCard().getManaValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Created Tarmogoyf contributes one green devotion")
    void createdTarmogoyfContributesGreenDevotion() {
        setUpEnchantedForest();
        int devotionBefore = gqs.getDevotionToColor(gd, player1.getId(), ManaColor.GREEN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getDevotionToColor(gd, player1.getId(), ManaColor.GREEN))
                .isEqualTo(devotionBefore + 1);
    }

    @Test
    @DisplayName("Token counts distinct types in both graveyards and shrinks when they empty")
    void countsBothGraveyardsIncludingKindredAndShrinks() {
        setUpEnchantedForest();
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Forest(), new TarmogoyfNest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Tarmogoyf");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);

        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, token)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's enchanted land creates the token for that opponent")
    void opponentsLandCreatesTokenForItsController() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new TarmogoyfNest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("A tapped land cannot pay the granted ability's tap cost")
    void tappedLandCannotActivate() {
        Permanent forest = setUpEnchantedForest();
        forest.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The granted ability requires green mana")
    void cannotActivateWithOnlyColorlessMana() {
        Permanent forest = setUpEnchantedForest();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent setUpEnchantedForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent nest = harness.addToBattlefieldAndReturn(player1, new TarmogoyfNest());
        nest.setAttachedTo(forest.getId());
        return forest;
    }
}
