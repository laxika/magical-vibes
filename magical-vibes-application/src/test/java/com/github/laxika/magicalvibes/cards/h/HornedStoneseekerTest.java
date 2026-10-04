package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.o.ObliteratingBolt;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HornedStoneseeker.class, WrathOfGod.class, ObliteratingBolt.class})
class HornedStoneseekerTest extends BaseCardTest {

    @Test
    @DisplayName("When Horned Stoneseeker enters, it creates a tapped Powerstone token")
    void entersCreatesTappedPowerstone() {
        harness.setHand(player1, List.of(new HornedStoneseeker()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
        assertThat(powerstones.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstones.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
    }

    @Test
    @DisplayName("When Horned Stoneseeker leaves, its controller sacrifices a Powerstone")
    void leavesSacrificesPowerstone() {
        harness.setHand(player1, List.of(new HornedStoneseeker()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Horned Stoneseeker")).isZero();
        assertThat(countPermanents(player1, "Powerstone")).isZero();
    }

    @Test
    @DisplayName("Exiling Horned Stoneseeker also triggers the Powerstone sacrifice")
    void exileSacrificesPowerstone() {
        Permanent seeker = harness.enterBattlefieldAndReturn(player2, new HornedStoneseeker());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0, seeker.getId());
        assertThat(countPermanents(player2, "Powerstone")).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Powerstone")).isZero();
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).contains("Horned Stoneseeker");
    }

    @Test
    @DisplayName("The controller can sacrifice a Powerstone created by another Stoneseeker")
    void choosesAnyControlledPowerstone() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new HornedStoneseeker());
        harness.passBothPriorities();
        Permanent firstPowerstone = findPermanents(player1, "Powerstone").getFirst();
        harness.enterBattlefieldAndReturn(player1, new HornedStoneseeker());
        harness.passBothPriorities();
        Permanent secondPowerstone = findPermanents(player1, "Powerstone").stream()
                .filter(p -> !p.getId().equals(firstPowerstone.getId())).findFirst().orElseThrow();
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0, first.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(secondPowerstone.getId()));

        assertThat(findPermanents(player1, "Powerstone")).containsExactly(firstPowerstone);
        assertThat(countPermanents(player1, "Horned Stoneseeker")).isEqualTo(1);
    }

    @Test
    @DisplayName("Leaving without a Powerstone does not sacrifice an opponent's Powerstone")
    void noControlledPowerstoneDoesNothing() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new HornedStoneseeker());
        harness.enterBattlefieldAndReturn(player2, new HornedStoneseeker());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0, seeker.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Powerstone")).isZero();
        assertThat(countPermanents(player2, "Powerstone")).isEqualTo(1);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The Powerstone produces mana immediately but cannot pay for a nonartifact spell")
    void powerstoneManaCannotCastNonartifactSpell() {
        harness.enterBattlefieldAndReturn(player1, new HornedStoneseeker());
        harness.passBothPriorities();
        Permanent powerstone = findPermanents(player1, "Powerstone").getFirst();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(powerstone);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.performUntapStep(player1);
        harness.activateAbility(player1, index, null, null);

        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        harness.setHand(player1, List.of(new HornedStoneseeker()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Horned Stoneseeker")).isEqualTo(1);
    }
}
