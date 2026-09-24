package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvenFogbringer;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({AvenFogbringer.class, BattleScreech.class, Brawn.class, KrosanVerge.class, SuntailHawk.class})
class BattleScreechTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Battle Screech creates two 1/1 white Bird tokens with flying")
    void createsTwoBirdTokens() {
        harness.setHand(player1, List.of(new BattleScreech()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        assertThat(findPermanents(player1, "Bird")).allSatisfy(bird -> {
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
            assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(bird.getCard().getPower()).isEqualTo(1);
            assertThat(bird.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Flashback taps three untapped white creatures and creates two more Birds")
    void flashbackTapsThreeWhiteCreaturesAndCreatesBirds() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Card spell = new BattleScreech();
        harness.setGraveyard(player1, List.of(spell));

        harness.castFlashbackWithTapCost(player1, 0,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Bird")).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Flashback rejects a nonwhite creature")
    void flashbackRequiresWhiteCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent nonwhite = harness.addToBattlefieldAndReturn(player1, new Brawn());
        Card spell = new BattleScreech();
        harness.setGraveyard(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(first.getId(), second.getId(), nonwhite.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required filter");

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        assertThat(nonwhite.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Flashback rejects a noncreature permanent")
    void flashbackRequiresCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());
        Card spell = new BattleScreech();
        harness.setGraveyard(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(first.getId(), second.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required filter");

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Flashback rejects tapped creatures and creatures controlled by an opponent")
    void flashbackRequiresUntappedCreaturesYouControl() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        first.tap();
        Card spell = new BattleScreech();
        harness.setGraveyard(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(second.getId(), third.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        assertThat(fourth.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);

        first.untap();
        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(first.getId(), second.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found on your battlefield");

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Duplicate flashback tap choices do not partially pay the cost")
    void duplicateFlashbackTapChoicesAreRejectedAtomically() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Card spell = new BattleScreech();
        harness.setGraveyard(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(first.getId(), first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Flashback rejects a nonwhite creature for its tap cost")
    void flashbackRejectsNonwhiteCreatureForTapCost() {
        Permanent blueCreature = addCreatureReady(player1, new AvenFogbringer());
        Permanent whiteCreature = addCreatureReady(player1, new SuntailHawk());
        Permanent anotherWhiteCreature = addCreatureReady(player1, new SuntailHawk());
        Card spell = new BattleScreech();
        harness.setGraveyard(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(blueCreature.getId(), whiteCreature.getId(), anotherWhiteCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tap target does not match the required filter");

        assertThat(blueCreature.isTapped()).isFalse();
        assertThat(whiteCreature.isTapped()).isFalse();
        assertThat(anotherWhiteCreature.isTapped()).isFalse();
        assertThat(birdTokensForJudReview()).isEmpty();
        harness.assertInGraveyard(player1, "Battle Screech");
    }

    @Test
    @DisplayName("Flashback rejects an already-tapped white creature for its tap cost")
    void flashbackRejectsAlreadyTappedWhiteCreatureForTapCost() {
        Permanent tappedWhiteCreature = addCreatureReady(player1, new SuntailHawk());
        tappedWhiteCreature.tap();
        Permanent whiteCreature = addCreatureReady(player1, new SuntailHawk());
        Permanent anotherWhiteCreature = addCreatureReady(player1, new SuntailHawk());
        Card spell = new BattleScreech();
        harness.setGraveyard(player1, List.of(spell));

        assertThatThrownBy(() -> harness.castFlashbackWithTapCost(player1, 0,
                List.of(tappedWhiteCreature.getId(), whiteCreature.getId(), anotherWhiteCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Permanent is already tapped");

        assertThat(tappedWhiteCreature.isTapped()).isTrue();
        assertThat(whiteCreature.isTapped()).isFalse();
        assertThat(anotherWhiteCreature.isTapped()).isFalse();
        assertThat(birdTokensForJudReview()).isEmpty();
        harness.assertInGraveyard(player1, "Battle Screech");
    }

    private List<Permanent> birdTokensForJudReview() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Bird"))
                .toList();
    }
}
