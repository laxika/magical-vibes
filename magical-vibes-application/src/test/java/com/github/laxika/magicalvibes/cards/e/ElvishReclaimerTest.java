package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvishReclaimerTest extends BaseCardTest {

    @Nested
    @DisplayName("Graveyard threshold")
    class GraveyardThresholdTests {

        @Test
        @DisplayName("Gets +2/+2 with three land cards in its controller's graveyard")
        void boostsWithThreeLandCardsInGraveyard() {
            Permanent reclaimer = addReclaimer(player1);
            int basePower = gqs.getEffectivePower(gd, reclaimer);
            int baseToughness = gqs.getEffectiveToughness(gd, reclaimer);

            harness.setGraveyard(player1, List.of(new Forest(), new Island(), new Plains()));

            assertThat(gqs.getEffectivePower(gd, reclaimer)).isEqualTo(basePower + 2);
            assertThat(gqs.getEffectiveToughness(gd, reclaimer)).isEqualTo(baseToughness + 2);
        }

        @Test
        @DisplayName("Does not boost with fewer than three land cards")
        void doesNotBoostBelowThreshold() {
            Permanent reclaimer = addReclaimer(player1);
            int basePower = gqs.getEffectivePower(gd, reclaimer);

            harness.setGraveyard(player1, List.of(new Forest(), new Island(), new GrizzlyBears()));

            assertThat(gqs.getEffectivePower(gd, reclaimer)).isEqualTo(basePower);
        }

        @Test
        @DisplayName("Does not count land cards in an opponent's graveyard")
        void ignoresOpponentGraveyard() {
            Permanent reclaimer = addReclaimer(player1);
            int basePower = gqs.getEffectivePower(gd, reclaimer);

            harness.setGraveyard(player2, List.of(new Forest(), new Island(), new Plains()));

            assertThat(gqs.getEffectivePower(gd, reclaimer)).isEqualTo(basePower);
        }
    }

    @Nested
    @DisplayName("Search activated ability")
    class SearchAbilityTests {

        @Test
        @DisplayName("Sacrifices a land and puts a fetched land onto the battlefield tapped")
        void sacrificesLandAndFetchesLandTapped() {
            addReclaimer(player1);
            harness.addToBattlefield(player1, new Forest());
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            List<Card> deck = gd.playerDecks.get(player1.getId());
            deck.clear();
            deck.addAll(List.of(new Island(), new GrizzlyBears()));

            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

            harness.assertInGraveyard(player1, "Forest");
            Permanent island = findPermanent(player1, "Island");
            assertThat(island).isNotNull();
            assertThat(island.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Cannot activate without a land to sacrifice")
        void cannotActivateWithoutLand() {
            addReclaimer(player1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private Permanent addReclaimer(Player player) {
        harness.addToBattlefield(player, new ElvishReclaimer());
        Permanent reclaimer = findPermanent(player, "Elvish Reclaimer");
        reclaimer.setSummoningSick(false);
        return reclaimer;
    }
}
