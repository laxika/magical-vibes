package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenjuOfTheSpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Genju of the Spires cannot enchant a non-Mountain land")
    void cannotEnchantNonMountain() {
        harness.addToBattlefield(player1, new Mountain()); // legal target so the spell is castable
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.setHand(player1, List.of(new GenjuOfTheSpires()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Mountain");
    }

    @Test
    @DisplayName("Activating {2} makes the enchanted Mountain a 6/1 red Spirit that is still a land")
    void activationAnimatesEnchantedMountain() {
        Permanent mountain = addMountainWithGenju();

        activateGenju();

        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(mountain.getEffectivePower()).isEqualTo(6);
        assertThat(mountain.getEffectiveToughness()).isEqualTo(1);
        assertThat(mountain.getTransientSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(mountain.getAnimatedColor()).isEqualTo(CardColor.RED);
        assertThat(mountain.getCard().getType()).isEqualTo(CardType.LAND);
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent mountain = addMountainWithGenju();
        activateGenju();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mountain)).isFalse();
    }

    @Test
    @DisplayName("Destroying the enchanted Mountain lets you return Genju from your graveyard to your hand")
    void returnsFromGraveyardWhenMountainDies() {
        Permanent mountain = addMountainWithGenju();

        destroyMountain(mountain);
        harness.passBothPriorities(); // resolve the "may return" trigger
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Genju of the Spires"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Genju of the Spires"));
    }

    @Test
    @DisplayName("Declining the trigger leaves Genju in the graveyard")
    void decliningLeavesGenjuInGraveyard() {
        Permanent mountain = addMountainWithGenju();

        destroyMountain(mountain);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Genju of the Spires"));
    }

    private Permanent addMountainWithGenju() {
        harness.addToBattlefield(player1, new Mountain());
        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.setHand(player1, List.of(new GenjuOfTheSpires()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, mountainId);
        harness.passBothPriorities();

        return findPermanent(player1, "Mountain");
    }

    private void activateGenju() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int genjuIndex = gd.playerBattlefields.get(player1.getId()).indexOf(
                findPermanent(player1, "Genju of the Spires"));
        harness.activateAbility(player1, genjuIndex, null, null);
        harness.passBothPriorities();
    }

    private void destroyMountain(Permanent mountain) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new StoneRain()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castSorcery(player2, 0, mountain.getId());
        harness.passBothPriorities(); // resolve Stone Rain
    }
}
