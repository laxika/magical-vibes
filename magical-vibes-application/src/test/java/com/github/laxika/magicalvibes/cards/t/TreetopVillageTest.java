package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TreetopVillage.class)
class TreetopVillageTest extends BaseCardTest {

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Treetop Village enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new TreetopVillage()));

        harness.playLand(player1, 0);

        Permanent village = findPermanent(player1, "Treetop Village");
        assertThat(village.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Treetop Village produces green mana")
    void tappingProducesGreenMana() {
        addVillageReady(player1);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("Activating ability puts Treetop Village's ability on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(village.getId());
    }

    @Test
    @DisplayName("Resolving ability makes it a 3/3 green Ape creature with trample")
    void resolvingAbilityMakesItA3x3WithTrample() {
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, village)).isTrue();
        assertThat(gqs.getEffectivePower(gd, village)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, village)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, village)).containsExactly(CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, village)).containsExactly(CardSubtype.APE);
        assertThat(gqs.hasKeyword(gd, village, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Treetop Village is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, village)).isTrue();
        assertThat(gqs.isCreature(gd, village)).isTrue();
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(village.isTapped()).isFalse();
    }

    // ===== Animation resets at end of turn =====

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, village)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, village)).isFalse();
        assertThat(gqs.isLand(gd, village)).isTrue();
        assertThat(gqs.hasKeyword(gd, village, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, village)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, village)).isEmpty();
    }

    // ===== Mana cost enforcement =====

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ability requires one generic and one green mana")
    void abilityRequiresOneGenericAndOneGreenMana() {
        addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can be activated while Treetop Village is tapped")
    void abilityCanBeActivatedWhileTapped() {
        Permanent village = addVillageReady(player1);
        harness.tapPermanent(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(village.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can be activated during another player's turn")
    void abilityCanBeActivatedDuringAnotherPlayersTurn() {
        addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Treetop Village is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent village = addVillageReady(player1);

        assertThat(gqs.isCreature(gd, village)).isFalse();
        assertThat(gqs.isLand(gd, village)).isTrue();
    }

    // ===== Helper methods =====

    private Permanent addVillageReady(Player player) {
        return addCreatureReady(player, new TreetopVillage());
    }
}
