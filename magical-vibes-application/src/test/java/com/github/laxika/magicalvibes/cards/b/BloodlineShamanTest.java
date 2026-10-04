package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GluttonousZombie;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodlineShaman.class, AvianChangeling.class, ElvishWarrior.class, Forest.class,
        GluttonousZombie.class, LeylineOfTheVoid.class})
class BloodlineShamanTest extends BaseCardTest {

    @Test
    @DisplayName("A creature card of the chosen type goes into its controller's hand")
    void matchingCreatureGoesToHand() {
        ElvishWarrior elf = new ElvishWarrior();
        activateAndChoose(elf, "ELF");

        assertThat(gd.playerHands.get(player1.getId())).contains(elf);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(elf);
    }

    @Test
    @DisplayName("A nonmatching creature card goes into its controller's graveyard")
    void nonmatchingCreatureGoesToGraveyard() {
        GluttonousZombie zombie = new GluttonousZombie();
        activateAndChoose(zombie, "ELF");

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(zombie);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(zombie);
    }

    @Test
    @DisplayName("A noncreature card goes into its controller's graveyard")
    void noncreatureGoesToGraveyard() {
        Forest forest = new Forest();
        activateAndChoose(forest, "BEAR");

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    @DisplayName("A Changeling creature card matches the chosen type")
    void changelingMatchesChosenType() {
        AvianChangeling changeling = new AvianChangeling();
        activateAndChoose(changeling, "ELF");

        assertThat(gd.playerHands.get(player1.getId())).contains(changeling);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(changeling);
    }

    @Test
    @DisplayName("Only the top card is revealed and moved")
    void onlyTopCardIsProcessed() {
        ElvishWarrior topCard = new ElvishWarrior();
        Forest cardBelowTop = new Forest();
        activateAndChoose(List.of(topCard, cardBelowTop), "ELF");

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cardBelowTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("An empty library is left empty")
    void emptyLibraryDoesNothing() {
        activateAndChoose(List.of(), "ELF");

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activation taps the Shaman and leaves the library untouched until resolution")
    void choosesTypeAndMovesCardOnlyDuringResolution() {
        var shaman = addCreatureReady(player1, new BloodlineShaman());
        ElvishWarrior elf = new ElvishWarrior();
        harness.setLibrary(player1, List.of(elf));

        harness.activateAbility(player1, 0, null, null);

        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elf);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(elf);

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elf);

        harness.handleListChoice(player1, "WARRIOR");

        assertThat(gd.playerHands.get(player1.getId())).contains(elf);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each activation permits a new creature type choice")
    void subsequentActivationChoosesNewType() {
        var firstShaman = addCreatureReady(player1, new BloodlineShaman());
        addCreatureReady(player1, new BloodlineShaman());
        ElvishWarrior elf = new ElvishWarrior();
        GluttonousZombie zombie = new GluttonousZombie();
        harness.setLibrary(player1, List.of(elf, zombie));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ZOMBIE");

        assertThat(firstShaman.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(elf, zombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(elf, zombie);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Shaman cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new BloodlineShaman());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("A tapped Shaman cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        var shaman = addCreatureReady(player1, new BloodlineShaman());
        shaman.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("An opponent's Leyline of the Void exiles a nonmatching revealed card")
    void graveyardMoveRespectsReplacementEffects() {
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        GluttonousZombie zombie = new GluttonousZombie();

        activateAndChoose(zombie, "ELF");

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(zombie);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(zombie);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(zombie);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void activateAndChoose(Card topCard, String subtype) {
        activateAndChoose(List.of(topCard), subtype);
    }

    private void activateAndChoose(List<Card> library, String subtype) {
        addCreatureReady(player1, new BloodlineShaman());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype);
    }
}
