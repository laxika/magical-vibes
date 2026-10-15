package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LindenTheSteadfastQueen;
import com.github.laxika.magicalvibes.cards.m.MaraleafPixie;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
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

@CardUsed({TheCircleOfLoyalty.class, LindenTheSteadfastQueen.class, YouthfulKnight.class, MaraleafPixie.class})
class TheCircleOfLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for Knights reduces the casting cost by one per Knight")
    void affinityForKnightsReducesCastingCost() {
        harness.addToBattlefield(player1, new YouthfulKnight());
        harness.setHand(player1, List.of(new TheCircleOfLoyalty()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity does not count non-Knights")
    void affinityDoesNotCountNonKnights() {
        harness.addToBattlefield(player1, new MaraleafPixie());
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
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new MaraleafPixie());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new MaraleafPixie());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a legendary spell creates a Knight token")
    void legendarySpellCreatesKnightToken() {
        harness.addToBattlefield(player1, new TheCircleOfLoyalty());
        harness.setHand(player1, List.of(new LindenTheSteadfastQueen()));
        harness.addMana(player1, ManaColor.WHITE, 3);

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

    @Test
    void affinityDoesNotCountOpponentsKnights() {
        harness.addToBattlefield(player2, new YouthfulKnight());
        harness.setHand(player1, List.of(new TheCircleOfLoyalty()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void affinityCanRemoveAllGenericManaButNotWhiteMana() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new YouthfulKnight());
        }
        harness.setHand(player1, List.of(new TheCircleOfLoyalty()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void castingCircleDoesNotTriggerItsOwnAbility() {
        harness.setHand(player1, List.of(new TheCircleOfLoyalty()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Circle of Loyalty");
        harness.assertNotOnBattlefield(player1, "Knight");
    }

    @Test
    void nonlegendarySpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new TheCircleOfLoyalty());
        harness.setHand(player1, List.of(new YouthfulKnight()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Knight");
    }

    @Test
    void opponentsLegendarySpellDoesNotCreateToken() {
        harness.addToBattlefield(player1, new TheCircleOfLoyalty());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LindenTheSteadfastQueen()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Knight");
        harness.assertNotOnBattlefield(player2, "Knight");
    }

    @Test
    void legendaryArtifactTriggersBeforeTheSpellResolves() {
        harness.addToBattlefield(player1, new TheCircleOfLoyalty());
        harness.setHand(player1, List.of(new TheCircleOfLoyalty()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        Permanent knight = findPermanent(player1, "Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
    }

    @Test
    void activationRequiresWhiteManaAndTapsCircle() {
        Permanent circle = harness.addToBattlefieldAndReturn(player1, new TheCircleOfLoyalty());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(circle.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        assertThat(circle.isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Knight")).hasSize(1);
    }
}
