package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BolassCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GryffRider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingedPortent.class, GrizzlyBears.class, GryffRider.class})
class WingedPortentTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast draws for each creature you control with flying")
    void normalCastDrawsForFlyingCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GryffRider());
        harness.setHand(player1, List.of(new WingedPortent()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addNormalMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cleave cast draws for each creature you control")
    void cleaveCastDrawsForAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GryffRider());
        harness.setHand(player1, List.of(new WingedPortent()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Normal cast draws nothing when only the opponent has flying creatures")
    void normalCastDoesNotCountOpposingFlyingCreatures() {
        harness.addToBattlefield(player2, new GryffRider());
        harness.setHand(player1, List.of(new WingedPortent()));
        harness.setLibrary(player1, List.of(new GryffRider()));
        addNormalMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cleave cast draws nothing when only the opponent controls creatures")
    void cleaveCastDoesNotCountOpposingCreatures() {
        harness.addToBattlefield(player2, new GryffRider());
        harness.setHand(player1, List.of(new WingedPortent()));
        harness.setLibrary(player1, List.of(new GryffRider()));
        addCleaveMana();

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Normal cast counts flying creatures at resolution")
    void normalCastCountsFlyingCreaturesAtResolution() {
        harness.addToBattlefield(player1, new GryffRider());
        harness.addToBattlefield(player2, new GryffRider());
        harness.setHand(player1, List.of(new WingedPortent()));
        harness.setLibrary(player1, List.of(new GryffRider(), new GryffRider(), new GryffRider()));
        addNormalMana();

        harness.castInstant(player1, 0);
        harness.addToBattlefield(player1, new GryffRider());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cleave cast counts creatures entering before resolution")
    void cleaveCastCountsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new GryffRider());
        harness.addToBattlefield(player2, new GryffRider());
        harness.setHand(player1, List.of(new WingedPortent()));
        harness.setLibrary(player1, List.of(new GryffRider(), new GryffRider(), new GryffRider()));
        addCleaveMana();

        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({WingedPortent.class, BolassCitadel.class, GryffRider.class, GrizzlyBears.class})
    @DisplayName("Paying life through Bolas's Citadel does not pay the cleave cost")
    void citadelLifePaymentDoesNotRemoveFlyingRestriction() {
        harness.addToBattlefield(player1, new BolassCitadel());
        harness.addToBattlefield(player1, new GryffRider());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new WingedPortent(), new GryffRider(), new GryffRider()));
        harness.setLife(player1, 20);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertLife(player1, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void addCleaveMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
