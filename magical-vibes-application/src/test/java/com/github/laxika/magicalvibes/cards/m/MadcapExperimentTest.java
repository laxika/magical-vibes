package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FiligreeFamiliar;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MadcapExperiment.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class,
        FiligreeFamiliar.class, PropheticPrism.class})
class MadcapExperimentTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the first revealed artifact onto the battlefield and deals damage for every revealed card")
    void findsArtifactAndDealsDamageForRevealedCards() {
        Card top = new Forest();
        Card artifact = new FountainOfYouth();
        Card bottom = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, artifact, bottom));
        harness.setHand(player1, List.of(new MadcapExperiment()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Forest");
    }

    @Test
    @DisplayName("Deals damage equal to the whole library when no artifact is found")
    void noArtifactDealsDamageForEntireLibrary() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new MadcapExperiment()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void emptyLibraryDealsNoDamage() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MadcapExperiment()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("An artifact on top counts as one revealed card and leaves later artifacts in the library")
    void artifactOnTopStopsRevealingImmediately() {
        Card first = new FountainOfYouth();
        Card second = new FountainOfYouth();
        Card bottom = new Forest();
        harness.setLibrary(player1, List.of(first, second, bottom));
        harness.setHand(player1, List.of(new MadcapExperiment()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard()).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, bottom);
    }

    @Test
    @DisplayName("Multiple revealed nonartifacts go below the untouched library cards")
    void revealedCardsGoToBottomWithoutChangingUntouchedCards() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card artifact = new FountainOfYouth();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(first, second, artifact, untouched));
        harness.setHand(player1, List.of(new MadcapExperiment()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 17);
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("An artifact creature's entry trigger resolves after Madcap Experiment's damage")
    void artifactCreatureEntryTriggerWaitsForSpellToFinish() {
        harness.setLibrary(player1, List.of(new FiligreeFamiliar(), new Forest()));
        harness.setHand(player1, List.of(new MadcapExperiment()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Filigree Familiar");
        harness.assertLife(player1, 19);
        harness.assertInGraveyard(player1, "Madcap Experiment");

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("A noncreature artifact's entry ability triggers and draws from the remaining library")
    void noncreatureArtifactEntryAbilityTriggers() {
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(new PropheticPrism(), remaining));
        harness.setHand(player1, List.of(new MadcapExperiment()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Prophetic Prism");
        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
