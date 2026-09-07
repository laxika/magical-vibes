package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheCircleOfLoyalty.class, AdelizTheCinderWind.class, BlackKnight.class, GrizzlyBears.class})
class TheCircleOfLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Knights reduces the casting cost by one per Knight")
    void affinityForKnightsReducesCastingCost() {
        harness.addToBattlefield(player1, new BlackKnight());
        harness.setHand(player1, List.of(new TheCircleOfLoyalty()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity does not count non-Knights")
    void affinityDoesNotCountNonKnights() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheCircleOfLoyalty()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures you control get +1/+1")
    void buffsCreaturesYouControl() {
        harness.addToBattlefield(player1, new TheCircleOfLoyalty());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a legendary spell creates a Knight token")
    void legendarySpellCreatesKnightToken() {
        harness.addToBattlefield(player1, new TheCircleOfLoyalty());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(knight.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The activated ability creates a Knight token")
    void activatedAbilityCreatesKnightToken() {
        harness.addToBattlefield(player1, new TheCircleOfLoyalty());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(knight.getCard().getSubtypes()).contains(CardSubtype.KNIGHT);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }
}
