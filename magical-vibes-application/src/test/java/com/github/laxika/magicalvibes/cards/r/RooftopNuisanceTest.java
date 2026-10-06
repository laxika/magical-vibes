package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SkyCrier;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RooftopNuisance.class, SkyCrier.class, Forest.class})
class RooftopNuisanceTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a target creature, skips its next untap, and draws a card")
    void tapsSkipsUntapAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkyCrier());
        Card drawn = new SkyCrier();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new RooftopNuisance()));
        addMana(player1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Casualty copies Rooftop Nuisance and sacrifices the chosen creature")
    void casualtyCopiesSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkyCrier());
        Permanent casualtyCreature = harness.addToBattlefieldAndReturn(player1, new SkyCrier());
        Card firstDraw = new SkyCrier();
        Card secondDraw = new SkyCrier();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new RooftopNuisance()));
        addMana(player1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), casualtyCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RooftopNuisance()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An already tapped creature skips only its controller's next untap step")
    void alreadyTappedCreatureSkipsOnlyNextUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkyCrier());
        target.setTapped(true);
        Card drawn = new SkyCrier();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new RooftopNuisance()));
        addMana(player1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Casualty copy can target a different creature and resolves before the original")
    void casualtyCopyCanChooseNewTarget() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new SkyCrier());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player1, new SkyCrier());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SkyCrier());
        Card firstDraw = new SkyCrier();
        Card secondDraw = new SkyCrier();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new RooftopNuisance()));
        addMana(player1);

        harness.castSorceryWithSacrifice(player1, 0, originalTarget.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();

        assertThat(copyTarget.isTapped()).isTrue();
        assertThat(originalTarget.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);

        harness.passBothPriorities();
        assertThat(originalTarget.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.performUntapStep(player1);
        assertThat(copyTarget.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(originalTarget.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing the original target still permits retargeting the casualty copy")
    void casualtyCopyResolvesWhenOriginalTargetWasSacrificed() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SkyCrier());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new SkyCrier());
        Card drawn = new SkyCrier();
        Card remaining = new SkyCrier();
        harness.setLibrary(player1, List.of(drawn, remaining));
        harness.setHand(player1, List.of(new RooftopNuisance()));
        addMana(player1);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(copyTarget.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertInGraveyard(player1, "Rooftop Nuisance");
    }

    @Test
    @DisplayName("An illegal sole target prevents drawing a card")
    void noDrawWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkyCrier());
        Card drawn = new SkyCrier();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new RooftopNuisance()));
        addMana(player1);

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player2, List.of(target.getCard()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Rooftop Nuisance");
    }

    @Test
    @DisplayName("Casualty cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkyCrier());
        harness.setHand(player1, List.of(new RooftopNuisance()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Casualty cannot sacrifice a noncreature")
    void cannotSacrificeNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SkyCrier());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new RooftopNuisance()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
