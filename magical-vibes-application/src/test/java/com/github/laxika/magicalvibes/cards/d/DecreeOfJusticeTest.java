package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GildedLight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecreeOfJustice.class, GildedLight.class})
class DecreeOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=2 creates two 4/4 flying Angels")
    void hardCastCreatesAngels() {
        harness.setHand(player1, List.of(new DecreeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, 2);

        List<Permanent> angels = findPermanents(player1, "Angel");
        assertThat(angels).hasSize(2);
        assertThat(angels).allSatisfy(angel -> {
            assertThat(angel.getCard().isToken()).isTrue();
            assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(angel.getCard().getSubtypes()).contains(CardSubtype.ANGEL);
            assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    @DisplayName("Cycling can pay X to create Soldiers and still draws a card")
    void cyclingPaysXCreatesSoldiersAndDraws() {
        harness.setHand(player1, List.of(new DecreeOfJustice()));
        harness.setLibrary(player1, List.of(new GildedLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 2);

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().isToken()).isTrue();
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
            assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, soldier, Keyword.FLYING)).isFalse();
        });
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Decree of Justice");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Gilded Light");
    }

    @Test
    @DisplayName("Cycling with X=0 creates no Soldiers and still draws a card")
    void cyclingDeclinesSoldiersAndDraws() {
        harness.setHand(player1, List.of(new DecreeOfJustice()));
        harness.setLibrary(player1, List.of(new GildedLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 0);

        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Decree of Justice");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Gilded Light");
    }

    @Test
    @DisplayName("Casting with X=0 creates no tokens and pays the fixed mana cost")
    void hardCastWithZeroCreatesNoTokens() {
        harness.setHand(player1, List.of(new DecreeOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Decree of Justice");
    }

    @Test
    @DisplayName("Soldiers are created before a separate cycling ability draws the card")
    void soldiersResolveBeforeCyclingDraw() {
        harness.setHand(player1, List.of(new DecreeOfJustice()));
        harness.setLibrary(player1, List.of(new GildedLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        harness.assertNotInHand(player1, "Gilded Light");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Gilded Light");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling with only its activation cost still draws without creating Soldiers")
    void cyclingWithNoManaForSoldiersStillDraws() {
        harness.setHand(player1, List.of(new DecreeOfJustice()));
        harness.setLibrary(player1, List.of(new GildedLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInHand(player1, "Gilded Light");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Gilded Light");
        harness.assertInGraveyard(player1, "Decree of Justice");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
