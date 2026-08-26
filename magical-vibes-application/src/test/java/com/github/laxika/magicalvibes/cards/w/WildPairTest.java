package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildPair.class, GrizzlyBears.class, WallOfEssence.class, HillGiant.class, Zombify.class})
class WildPairTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature offers a search for an exact total power and toughness match")
    void castingCreatureOffersExactTotalMatch() {
        castWildPair();
        Card matchingCreature = new WallOfEssence();
        harness.setLibrary(player1, List.of(matchingCreature, new HillGiant()));
        castGrizzlyBears();

        resolveWildPairMayPrompt();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCreature);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player1, "Wall of Essence")).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the search leaves the library unchanged")
    void decliningSearchDoesNothing() {
        castWildPair();
        Card matchingCreature = new WallOfEssence();
        harness.setLibrary(player1, List.of(matchingCreature));
        castGrizzlyBears();

        resolveWildPairMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(matchingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == matchingCreature);
    }

    @Test
    @DisplayName("A creature returned from a graveyard does not trigger Wild Pair")
    void creatureReturnedFromGraveyardDoesNotTrigger() {
        castWildPair();
        Card returnedCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, returnedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == returnedCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    private void castWildPair() {
        harness.setHand(player1, List.of(new WildPair()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    private void resolveWildPairMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
