package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarduMonument.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class})
class MarduMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("The enter-the-battlefield ability searches for a Mountain, Plains, or Swamp")
    void searchesForAMarduBasicLand() {
        harness.setHand(player1, List.of(new MarduMonument()));
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new Mountain(), new Plains(), new Swamp(), new Forest(), new Island())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Mountain", "Plains", "Swamp");

        String chosenName = search.params().cards().getFirst().getName();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains(chosenName);
    }

    @Test
    @DisplayName("Sacrificing the monument creates three temporary menace and haste Warriors")
    void sacrificeCreatesThreeWarriorsWithTemporaryKeywords() {
        harness.addToBattlefield(player1, new MarduMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> warriors = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Warrior"))
                .toList();
        assertThat(warriors).hasSize(3).allSatisfy(warrior -> {
            assertThat(warrior.getEffectivePower()).isEqualTo(1);
            assertThat(warrior.getEffectiveToughness()).isEqualTo(1);
            assertThat(warrior.hasKeyword(Keyword.MENACE)).isTrue();
            assertThat(warrior.hasKeyword(Keyword.HASTE)).isTrue();
        });

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warriors).allSatisfy(warrior -> {
            assertThat(warrior.hasKeyword(Keyword.MENACE)).isFalse();
            assertThat(warrior.hasKeyword(Keyword.HASTE)).isFalse();
        });
    }

    @Test
    @DisplayName("The token ability can be activated only at sorcery speed")
    void onlyAtSorcerySpeed() {
        harness.addToBattlefield(player1, new MarduMonument());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
