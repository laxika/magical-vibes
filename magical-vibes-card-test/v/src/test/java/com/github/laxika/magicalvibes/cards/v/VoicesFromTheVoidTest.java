package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientZiggurat;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoicesFromTheVoid.class, GrizzlyBears.class, HillGiant.class,
        LightningBolt.class, Plains.class, Island.class, Swamp.class, Mountain.class,
        Forest.class, AncientZiggurat.class})
class VoicesFromTheVoidTest extends BaseCardTest {

    private void castAtPlayer2() {
        harness.setHand(player1, List.of(new VoicesFromTheVoid()));
        harness.addMana(player1, ManaColor.BLACK, 5); // {4}{B}
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    @Test
    @DisplayName("Domain 2: target player discards a card for each of the two basic land types controlled")
    void discardsPerBasicLandType() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new HillGiant(), new LightningBolt())));

        castAtPlayer2();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Duplicate basic land types count only once toward the discard count")
    void duplicateTypesCountOnce() {
        // Two Plains + one Island = 2 basic land types, so the target discards two.
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new HillGiant(), new LightningBolt())));

        castAtPlayer2();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Controlling no basic land types makes the target discard nothing")
    void noBasicLandTypesDiscardsNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));

        castAtPlayer2();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void canTargetTheCaster() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new VoicesFromTheVoid(), new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void usesCasterLandsAtResolutionAndIgnoresTargetLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player2, List.of(new GrizzlyBears(), new HillGiant(), new LightningBolt()));
        harness.setHand(player1, List.of(new VoicesFromTheVoid()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, player2.getId());

        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void targetLandsDoNotContributeToDomain() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        castAtPlayer2();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void discardsEntireHandWhenDomainExceedsHandSize() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        castAtPlayer2();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void emptyTargetHandDoesNotRequireAChoice() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player2, List.of());

        castAtPlayer2();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void allFiveBasicLandTypesCauseFiveDiscards() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player2, List.of(new GrizzlyBears(), new HillGiant(), new LightningBolt(),
                new Plains(), new Island(), new Swamp()));

        castAtPlayer2();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(5);
        for (int i = 0; i < 5; i++) {
            harness.handleCardChosen(player2, 0);
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        harness.assertInHand(player2, "Swamp");
    }

    @Test
    void landWithoutBasicLandTypesDoesNotIncreaseDomain() {
        harness.addToBattlefield(player1, new AncientZiggurat());
        harness.setHand(player2, List.of(new GrizzlyBears()));

        castAtPlayer2();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
