package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LayClaim.class, Demystify.class, Forest.class, GrizzlyBears.class})
class LayClaimTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Lay Claim steals opponent's creature")
    void resolvingStealsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.stolenCreatures).containsEntry(bears.getId(), player2.getId());
    }

    @Test
    @DisplayName("Resolving Lay Claim steals a noncreature permanent (a land)")
    void resolvingStealsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(forest.getId()));
        assertThat(gd.stolenCreatures).containsEntry(forest.getId(), player2.getId());
    }

    @Test
    @DisplayName("Permanent returns to owner when Lay Claim is destroyed")
    void permanentReturnsWhenLayClaimDestroyed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent layClaimPerm = findPermanent(player1, "Lay Claim");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, layClaimPerm.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(bears.getId());
    }

    @Test
    @DisplayName("Cycling discards Lay Claim and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new LayClaim()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lay Claim");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A stolen land can immediately tap for mana")
    void stolenLandCanTapForMana() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lay Claim can enchant its controller's own permanent")
    void canEnchantOwnPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Lay Claim").getAttachedTo()).isEqualTo(forest.getId());
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Cycling discards as a cost before the draw resolves")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new LayClaim()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Lay Claim");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cycling cannot discard Lay Claim without paying two mana")
    void cyclingRequiresFullManaCost() {
        harness.setHand(player1, List.of(new LayClaim()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Lay Claim");
        harness.assertNotInGraveyard(player1, "Lay Claim");
        assertThat(gd.stack).isEmpty();
    }
}
