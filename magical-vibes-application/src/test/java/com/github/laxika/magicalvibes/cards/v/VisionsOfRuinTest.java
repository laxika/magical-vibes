package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VisionsOfRuin.class, FountainOfYouth.class, GrizzlyBears.class, EdgarMarkov.class})
@DisplayName("Visions of Ruin")
class VisionsOfRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices an artifact and you create a Treasure")
    void sacrificesArtifactAndCreatesTreasure() {
        harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castVisionsOfRuin();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertTreasureCount(1);
    }

    @Test
    @DisplayName("Does not sacrifice a non-artifact permanent")
    void ignoresNonArtifactPermanents() {
        addCreatureReady(player2, new GrizzlyBears());

        castVisionsOfRuin();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertTreasureCount(0);
    }

    @Test
    @DisplayName("The opponent chooses which artifact to sacrifice")
    void opponentChoosesArtifact() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castVisionsOfRuin();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(first.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(second.getId()));
        assertTreasureCount(1);
    }

    @Test
    @DisplayName("Flashback is reduced by the greatest owned commander")
    void flashbackUsesCommanderReduction() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        VisionsOfRuin spell = new VisionsOfRuin();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertTreasureCount(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private void castVisionsOfRuin() {
        harness.setHand(player1, List.of(new VisionsOfRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }

    private void assertTreasureCount(int expected) {
        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(expected);
        assertThat(treasures).allSatisfy(treasure -> {
            assertThat(treasure.getCard().isToken()).isTrue();
            assertThat(treasure.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        });
    }
}
