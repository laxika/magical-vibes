package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FishersTalent.class, Forest.class, GrizzlyBears.class})
class FishersTalentTest extends BaseCardTest {

    @Test
    @DisplayName("At level 1, revealing a land creates a Fish and then draws it")
    void levelOneCreatesFishAndDrawsRevealedLand() {
        harness.addToBattlefield(player1, new FishersTalent());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent fish = findPermanent(player1, "Fish");
        assertThat(fish.getCard().getSubtypes()).containsExactly(CardSubtype.FISH);
        assertThat(fish.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("Declining the land reveal still draws a card")
    void decliningRevealStillDraws() {
        harness.addToBattlefield(player1, new FishersTalent());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Fish")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("Level 2 replaces Fish tokens with Sharks")
    void levelTwoCreatesSharkTokens() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new FishersTalent());
        levelUpToTwo(talent);
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fish")).isEmpty();
        Permanent shark = findPermanent(player1, "Shark");
        assertThat(shark.getCard().getSubtypes()).containsExactly(CardSubtype.SHARK);
        assertThat(shark.getEffectivePower()).isEqualTo(3);
        assertThat(shark.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Level 3 replaces the upgraded Shark token with an Octopus")
    void levelThreeCreatesOctopusTokens() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new FishersTalent());
        levelUpToThree(talent);
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Fish")).isEmpty();
        assertThat(findPermanents(player1, "Shark")).isEmpty();
        Permanent octopus = findPermanent(player1, "Octopus");
        assertThat(octopus.getCard().getSubtypes()).containsExactly(CardSubtype.OCTOPUS);
        assertThat(octopus.getEffectivePower()).isEqualTo(8);
        assertThat(octopus.getEffectiveToughness()).isEqualTo(8);
    }

    private void levelUpToTwo(Permanent talent) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, battlefieldIndex(talent), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent talent) {
        levelUpToTwo(talent);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(talent), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent talent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(talent);
    }
}
