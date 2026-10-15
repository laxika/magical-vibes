package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YawgmothsWill;
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

@CardUsed({VisionsOfRuin.class, FountainOfYouth.class, GrizzlyBears.class, EdgarMarkov.class,
        YawgmothsWill.class})
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

    @Test
    @DisplayName("Your own artifacts are not sacrificed")
    void leavesControllersArtifactsAlone() {
        harness.addToBattlefield(player1, new FountainOfYouth());

        castVisionsOfRuin();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        assertTreasureCount(0);
    }

    @Test
    @DisplayName("Flashback without a commander costs eight generic and two red mana")
    void flashbackWithoutCommanderPaysFullCost() {
        assertFlashbackPayment(8);
    }

    @Test
    @DisplayName("An owned commander on an opponent's battlefield still reduces flashback")
    void ownedCommanderControlledByOpponentReducesFlashback() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        harness.addToBattlefield(player2, commander);

        assertFlashbackPayment(2);
    }

    @Test
    @DisplayName("An opponent's commander does not reduce your flashback cost")
    void opponentsCommanderDoesNotReduceFlashback() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player2.getId(), commander);
        harness.addToBattlefield(player1, commander);

        assertFlashbackPayment(8);
    }

    @Test
    @DisplayName("An owned commander in the graveyard does not reduce flashback")
    void commanderInGraveyardDoesNotReduceFlashback() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        VisionsOfRuin spell = new VisionsOfRuin();
        harness.setGraveyard(player1, List.of(spell, commander));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castFlashback(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertInGraveyard(player1, "Edgar Markov");
        assertTreasureCount(0);
    }

    @Test
    @DisplayName("A commander does not reduce the cost when casting from hand")
    void commanderDoesNotReduceNormalCast() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);

        harness.castFromHand(player1, new VisionsOfRuin(), "{3}{R}");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Visions of Ruin");
        assertTreasureCount(0);
    }

    @Test
    @DisplayName("Casting with Yawgmoth's Will receives no flashback cost reduction")
    void commanderDoesNotReduceOtherGraveyardCastingCosts() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        VisionsOfRuin spell = new VisionsOfRuin();
        harness.setGraveyard(player1, List.of(spell));
        harness.castFromHand(player1, new YawgmothsWill(), "{2}{B}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertTreasureCount(0);
    }

    private void assertFlashbackPayment(int genericMana) {
        VisionsOfRuin spell = new VisionsOfRuin();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);

        harness.castFlashback(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertTreasureCount(0);
    }

    private void castVisionsOfRuin() {
        harness.castFromHand(player1, new VisionsOfRuin(), "{3}{R}");
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
