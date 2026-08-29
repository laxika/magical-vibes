package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }
}
