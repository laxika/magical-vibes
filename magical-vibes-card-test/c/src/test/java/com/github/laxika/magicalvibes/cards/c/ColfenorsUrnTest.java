package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColfenorsUrn.class, GiantSpider.class, GrizzlyBears.class, WrathOfGod.class, GloriousAnthem.class})
class ColfenorsUrnTest extends BaseCardTest {

    @Nested
    @DisplayName("Toughness 4+ death trigger")
    @CardUsed({ColfenorsUrn.class, GiantSpider.class, GrizzlyBears.class, WrathOfGod.class})
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
            harness.assertNotInGraveyard(player1, "Giant Spider");
        }

        @Test
        @DisplayName("Declining leaves the creature in the graveyard")
        void decliningLeavesInGraveyard() {
            Permanent urn = addUrn();
            harness.addToBattlefield(player1, new GiantSpider());
            castWrath();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.getCardsExiledByPermanent(urn.getId())).isEmpty();
            harness.assertInGraveyard(player1, "Giant Spider");
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

    @Test
    @CardUsed({ColfenorsUrn.class, GrizzlyBears.class, GloriousAnthem.class, WrathOfGod.class})
    void staticBonusesCountTowardToughnessAtDeath() {
        Permanent urn = addUrn();
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castWrath();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getCardsExiledByPermanent(urn.getId()))
                .extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    void ownedCreatureDyingUnderOpponentControlTriggers() {
        Permanent urn = addUrn();
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        gd.stolenCreatures.put(spider.getId(), player1.getId());
        recordControlEffect(spider, player2);
        castWrath();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getCardsExiledByPermanent(urn.getId())).containsExactly(spider.getCard());
    }

    @Test
    void opponentOwnedCreatureDoesNotTriggerWhenControlledByYou() {
        addUrn();
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        gd.stolenCreatures.put(spider.getId(), player2.getId());
        recordControlEffect(spider, player1);
        castWrath();

        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void creatureRemovedFromGraveyardCannotBeExiled() {
        Permanent urn = addUrn();
        harness.addToBattlefield(player1, new GiantSpider());
        castWrath();
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(urn.getId())).isEmpty();
    }

    @Test
    void changingControllerAfterTriggerPreventsSacrificeAndReturn() {
        Permanent urn = addUrn();
        exileWithUrn(urn, 3);
        fireEndStep();
        gd.playerBattlefields.get(player1.getId()).remove(urn);
        gd.playerBattlefields.get(player2.getId()).add(urn);
        gd.stolenCreatures.put(urn.getId(), player1.getId());
        recordControlEffect(urn, player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Colfenor's Urn");
        assertThat(gd.getCardsExiledByPermanent(urn.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Giant Spider");
    }

    @Test
    void missingUrnPreventsReturn() {
        Permanent urn = addUrn();
        exileWithUrn(urn, 3);
        fireEndStep();
        gd.playerBattlefields.get(player1.getId()).remove(urn);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(urn.getId())).hasSize(3);
        harness.assertNotOnBattlefield(player1, "Giant Spider");
    }

    @Test
    void opponentEndStepReturnsAllFourCardsToTheirOwners() {
        Permanent urn = addUrn();
        exileWithUrn(urn, 3);
        gd.addToExile(player2.getId(), new GiantSpider(), urn.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Colfenor's Urn");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player2, "Giant Spider");
        assertThat(gd.getCardsExiledByPermanent(urn.getId())).isEmpty();
    }

    @Nested
    @DisplayName("End step sacrifice-and-return")
    @CardUsed({ColfenorsUrn.class, GiantSpider.class})
    class EndStep {

        @Test
        @DisplayName("With three cards exiled, the artifact is sacrificed and the cards return")
        void threeExiledSacrificesAndReturns() {
            Permanent urn = addUrn();
            exileWithUrn(urn, 3);

            fireEndStep();

            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Colfenor's Urn");
            assertThat(countPermanents(player1, "Giant Spider")).isEqualTo(3);
            assertThat(gd.getCardsExiledByPermanent(urn.getId())).isEmpty();
        }

        @Test
        @DisplayName("With only two cards exiled, nothing happens")
        void twoExiledDoesNothing() {
            Permanent urn = addUrn();
            exileWithUrn(urn, 2);

            fireEndStep();

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player1, "Colfenor's Urn");
            assertThat(gd.getCardsExiledByPermanent(urn.getId())).hasSize(2);
        }
    }

    private Permanent addUrn() {
        return harness.addToBattlefieldAndReturn(player1, new ColfenorsUrn());
    }

    private void castWrath() {
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
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
    private void recordControlEffect(Permanent permanent, com.github.laxika.magicalvibes.model.Player controller) {
        gd.addFloatingEffect(new FloatingContinuousEffect(
                java.util.UUID.randomUUID(), "Control setup", null, controller.getId(),
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
    }
}
