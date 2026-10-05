package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshalingCry.class, BlindPhantasm.class})
class MarshalingCryTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+1 and vigilance until end of turn")
    void boostsOwnCreaturesAndGrantsVigilanceUntilEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new BlindPhantasm());
        Permanent otherOwnCreature = addCreatureReady(player1, new BlindPhantasm());
        Permanent opposingCreature = addCreatureReady(player2, new BlindPhantasm());
        harness.setHand(player1, List.of(new MarshalingCry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherOwnCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherOwnCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, otherOwnCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution are not affected")
    void doesNotAffectCreaturesEnteringAfterResolution() {
        addCreatureReady(player1, new BlindPhantasm());
        harness.setHand(player1, List.of(new MarshalingCry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent enteredLater = addCreatureReady(player1, new BlindPhantasm());

        assertThat(gqs.getEffectivePower(gd, enteredLater)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enteredLater)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, enteredLater, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Flashback applies the same effect and exiles Marshaling Cry")
    void flashbackAppliesEffectAndExilesSpell() {
        Permanent ownCreature = addCreatureReady(player1, new BlindPhantasm());
        harness.setGraveyard(player1, List.of(new MarshalingCry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        harness.assertNotInGraveyard(player1, "Marshaling Cry");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Marshaling Cry"));
    }

    @Test
    @DisplayName("Cycling discards Marshaling Cry and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MarshalingCry()));
        harness.setLibrary(player1, List.of(new BlindPhantasm()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Marshaling Cry");
        harness.assertInHand(player1, "Blind Phantasm");
    }

    @Test
    @DisplayName("Cycling discards immediately without boosting creatures, then permits flashback")
    void cyclingDiscardsAsCostAndAllowsFlashbackAfterDrawing() {
        Permanent creature = addCreatureReady(player1, new BlindPhantasm());
        harness.setHand(player1, List.of(new MarshalingCry()));
        harness.setLibrary(player1, List.of(new BlindPhantasm()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Marshaling Cry");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Blind Phantasm");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        harness.assertNotInGraveyard(player1, "Marshaling Cry");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Marshaling Cry"));
    }

    @Test
    @DisplayName("Casting and flashing back in the same turn stacks the boosts")
    void normalCastThenFlashbackStacksBoosts() {
        Permanent creature = addCreatureReady(player1, new BlindPhantasm());
        harness.setHand(player1, List.of(new MarshalingCry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertInGraveyard(player1, "Marshaling Cry");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }
}
