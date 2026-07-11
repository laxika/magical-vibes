package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ColfenorsUrnTest extends BaseCardTest {

    @Nested
    @DisplayName("Toughness 4+ death trigger")
    class DeathTrigger {

        @Test
        @DisplayName("A dying creature with toughness 4 prompts the may-exile ability")
        void toughFourDyingPromptsMay() {
            addUrn();
            harness.addToBattlefield(player1, new GiantSpider());
            castWrath();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        }

        @Test
        @DisplayName("Accepting exiles the creature, tracked with the artifact")
        void acceptingExilesTrackedWithArtifact() {
            Permanent urn = addUrn();
            harness.addToBattlefield(player1, new GiantSpider());
            castWrath();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.getCardsExiledByPermanent(urn.getId()))
                    .extracting(Card::getName).containsExactly("Giant Spider");
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Giant Spider"));
        }

        @Test
        @DisplayName("Declining leaves the creature in the graveyard")
        void decliningLeavesInGraveyard() {
            Permanent urn = addUrn();
            harness.addToBattlefield(player1, new GiantSpider());
            castWrath();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.getCardsExiledByPermanent(urn.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Giant Spider"));
        }

        @Test
        @DisplayName("A dying creature with toughness below 4 does not trigger")
        void toughnessBelowFourDoesNotTrigger() {
            addUrn();
            harness.addToBattlefield(player1, new GrizzlyBears());
            castWrath();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        }
    }

    @Nested
    @DisplayName("End step sacrifice-and-return")
    class EndStep {

        @Test
        @DisplayName("With three cards exiled, the artifact is sacrificed and the cards return")
        void threeExiledSacrificesAndReturns() {
            Permanent urn = addUrn();
            exileWithUrn(urn, 3);

            fireEndStep();

            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Colfenor's Urn"));
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .filteredOn(p -> p.getCard().getName().equals("Giant Spider"))
                    .hasSize(3);
            assertThat(gd.getCardsExiledByPermanent(urn.getId())).isEmpty();
        }

        @Test
        @DisplayName("With only two cards exiled, nothing happens")
        void twoExiledDoesNothing() {
            Permanent urn = addUrn();
            exileWithUrn(urn, 2);

            fireEndStep();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Colfenor's Urn"));
            assertThat(gd.getCardsExiledByPermanent(urn.getId())).hasSize(2);
        }
    }

    // ===== Helpers =====

    private Permanent addUrn() {
        return harness.addToBattlefieldAndReturn(player1, new ColfenorsUrn());
    }

    private void castWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve Wrath — creatures die
        harness.passBothPriorities(); // resolve death may-effect → prompt (if any)
    }

    private void exileWithUrn(Permanent urn, int count) {
        for (int i = 0; i < count; i++) {
            gd.addToExile(player1.getId(), new GiantSpider(), urn.getId());
        }
    }

    private void fireEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }
}
