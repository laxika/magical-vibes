package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.f.FarAway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScionOfVituGhazi.class, BeaconOfUnrest.class, FarAway.class})
class ScionOfVituGhaziTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand: creates a Bird token, then populate copies it")
    void castFromHandCreatesBirdAndPopulates() {
        harness.setHand(player1, List.of(new ScionOfVituGhazi()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // The Bird is the controller's only creature token, so the populate choice is forced.
        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Permanent> birds = birdsOf(player1);
        assertThat(birds).hasSize(2);
        assertThat(birds).allSatisfy(bird -> {
            assertThat(bird.getCard().isToken()).isTrue();
            assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Cast from hand: with another creature token the controller chooses what populate copies")
    void controllerChoosesWhichTokenToCopy() {
        harness.addToBattlefield(player1, soldierToken());
        harness.setHand(player1, List.of(new ScionOfVituGhazi()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Soldier Token"));

        assertThat(countOf(player1, "Soldier Token")).isEqualTo(2);
        assertThat(birdsOf(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Entering from the graveyard rather than a hand cast makes no token")
    void enteringNotFromHandCreatesNothing() {
        ScionOfVituGhazi target = new ScionOfVituGhazi();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Scion of Vitu-Ghazi");
        assertThat(birdsOf(player1)).isEmpty();
    }

    @Test
    @DisplayName("Populate may copy the new Bird even when another creature token exists")
    void canChooseNewBirdInsteadOfExistingToken() {
        harness.addToBattlefield(player1, soldierToken());
        harness.setHand(player1, List.of(new ScionOfVituGhazi()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Bird"));

        assertThat(birdsOf(player1)).hasSize(2);
        assertThat(countOf(player1, "Soldier Token")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Populate excludes opponents' tokens and the nontoken Scion")
    void onlyControllersCreatureTokensCanBePopulated() {
        harness.addToBattlefield(player2, soldierToken());
        harness.setHand(player1, List.of(new ScionOfVituGhazi()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(birdsOf(player1)).hasSize(2);
        assertThat(countOf(player1, "Scion of Vitu-Ghazi")).isEqualTo(1);
        assertThat(countOf(player1, "Soldier Token")).isZero();
        assertThat(countOf(player2, "Soldier Token")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The hand-cast trigger creates and populates a Bird after Scion leaves")
    void triggerResolvesAfterSourceReturnsToHand() {
        harness.setHand(player1, List.of(new ScionOfVituGhazi()));
        harness.setHand(player2, List.of(new FarAway()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(birdsOf(player1)).isEmpty();
        harness.castInstant(player2, 0, 0, harness.getPermanentId(player1, "Scion of Vitu-Ghazi"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Scion of Vitu-Ghazi");
        harness.assertNotOnBattlefield(player1, "Scion of Vitu-Ghazi");
        assertThat(birdsOf(player1)).isEmpty();
        harness.passBothPriorities();

        assertThat(birdsOf(player1)).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private List<Permanent> birdsOf(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> "Bird".equals(p.getCard().getName()))
                .toList();
    }

    private long countOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .count();
    }

    private static Card soldierToken() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
