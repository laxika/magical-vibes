package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CoolButRude.class, GrizzlyBears.class})
class CoolButRudeTest extends BaseCardTest {

    @Test
    @DisplayName("When you attack, you may discard a card to draw a card")
    void attackMayDiscardAndDraw() {
        castCoolButRude();
        Card discarded = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("At level 2, discarding deals 2 damage to each opponent")
    void levelTwoDamagesOpponentsWhenDiscarding() {
        Permanent rude = castCoolButRude();
        levelUp(rude, 0);
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Reaching level 3 searches for a card, then randomly discards")
    void levelThreeSearchesAndDiscards() {
        Permanent rude = castCoolButRude();
        levelUp(rude, 0);
        Card searched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(searched));
        levelUp(rude, 1);

        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(searched);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    private Permanent castCoolButRude() {
        harness.setHand(player1, List.of(new CoolButRude()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Cool but Rude");
    }

    private void levelUp(Permanent rude, int abilityIndex) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int rudeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rude);
        harness.activateAbility(player1, rudeIndex, abilityIndex, null, null);
        harness.passBothPriorities();
    }
}
