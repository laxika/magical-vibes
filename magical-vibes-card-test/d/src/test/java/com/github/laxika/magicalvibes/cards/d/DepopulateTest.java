package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BrokersAscendancy;
import com.github.laxika.magicalvibes.cards.s.SnoopingNewsie;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZurgoAndOjutai;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Depopulate.class, FountainOfYouth.class, GrizzlyBears.class, ZurgoAndOjutai.class, SnoopingNewsie.class, BrokersAscendancy.class})
class DepopulateTest extends BaseCardTest {

    @Test
    @DisplayName("Each player with a multicolored creature draws before all creatures are destroyed")
    void qualifyingPlayersDrawBeforeCreatureWipe() {
        harness.addToBattlefield(player1, new ZurgoAndOjutai());
        harness.addToBattlefield(player2, new ZurgoAndOjutai());
        harness.setHand(player1, List.of(new Depopulate()));
        harness.setHand(player2, List.of());
        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Zurgo and Ojutai");
        harness.assertNotOnBattlefield(player2, "Zurgo and Ojutai");
    }

    @Test
    @DisplayName("Players without a multicolored creature do not draw")
    void nonQualifyingPlayersDoNotDraw() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ZurgoAndOjutai());
        harness.setHand(player1, List.of(new Depopulate()));
        harness.setHand(player2, List.of());
        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Depopulate leaves noncreature permanents alone")
    void leavesNoncreaturesAlone() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Depopulate()));
        harness.setHand(player2, List.of());
        castAndResolve();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("Multiple multicolored creatures still give their controller only one card")
    void multipleQualifyingCreaturesDrawOnlyOneCard() {
        harness.addToBattlefield(player1, new SnoopingNewsie());
        harness.addToBattlefield(player1, new SnoopingNewsie());
        harness.setHand(player1, List.of(new Depopulate()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new SnoopingNewsie(), new SnoopingNewsie()));
        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Snooping Newsie");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof SnoopingNewsie).hasSize(2);
    }

    @Test
    @DisplayName("Multicolored noncreature permanents do not qualify their controller for a draw")
    void multicoloredNoncreaturesDoNotQualify() {
        harness.addToBattlefield(player1, new BrokersAscendancy());
        harness.addToBattlefield(player2, new SnoopingNewsie());
        harness.setHand(player1, List.of(new Depopulate()));
        harness.setHand(player2, List.of());
        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Brokers Ascendancy");
        harness.assertNotOnBattlefield(player2, "Snooping Newsie");
    }

    @Test
    @DisplayName("No player draws when there are no creatures")
    void emptyBattlefieldsDoNotDraw() {
        harness.setHand(player1, List.of(new Depopulate()));
        harness.setHand(player2, List.of());
        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Depopulate");
    }

    private void castAndResolve() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
