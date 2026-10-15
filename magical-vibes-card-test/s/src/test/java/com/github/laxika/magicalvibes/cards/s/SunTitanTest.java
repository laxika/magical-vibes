package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.w.WallOfFrost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunTitan.class, RuneclawBear.class, Fog.class, StoneGolem.class, Plains.class, Pacifism.class, WallOfFrost.class})
class SunTitanTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB trigger")
    @CardUsed({SunTitan.class, RuneclawBear.class, Fog.class, StoneGolem.class, Plains.class, Pacifism.class, WallOfFrost.class})
    class ETBTrigger {

        @Test
        @DisplayName("Casting Sun Titan triggers may ability prompt")
        void etbTriggersMayPrompt() {
            harness.setGraveyard(player1, List.of(new RuneclawBear()));
            castSunTitan();
            harness.passBothPriorities();
            chooseGraveyardTarget(0);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting may and resolving returns permanent with MV ≤ 3 to battlefield")
        void returnsPermanentWithLowManaValue() {
            Card bears = new RuneclawBear();
            harness.setGraveyard(player1, List.of(bears));
            castSunTitan();
            harness.passBothPriorities();
            chooseGraveyardTarget(0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();

            harness.assertOnBattlefield(player1, "Runeclaw Bear");
            harness.assertNotInGraveyard(player1, "Runeclaw Bear");
        }

        @Test
        @DisplayName("Can return a land (permanent with MV 0) to battlefield")
        void returnsLandToBattlefield() {
            Card plains = new Plains();
            harness.setGraveyard(player1, List.of(plains));
            castSunTitan();
            harness.passBothPriorities();
            chooseGraveyardTarget(0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertOnBattlefield(player1, "Plains");
        }

        @Test
        @DisplayName("Cannot return non-permanent card (instant) from graveyard")
        void cannotReturnNonPermanent() {
            harness.setGraveyard(player1, List.of(new Fog()));
            castSunTitan();
            harness.passBothPriorities();

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Cannot return permanent with MV > 3 from graveyard")
        void cannotReturnHighManaValuePermanent() {
            harness.setGraveyard(player1, List.of(new StoneGolem()));
            castSunTitan();
            harness.passBothPriorities();

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Declining may ability does not return anything")
        void decliningMaySkipsReturn() {
            Card bears = new RuneclawBear();
            harness.setGraveyard(player1, List.of(bears));
            castSunTitan();
            harness.passBothPriorities();
            chooseGraveyardTarget(0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();

            harness.assertInGraveyard(player1, "Runeclaw Bear");
        }

        @Test
        @DisplayName("ETB ability cannot remain on the stack without a legal graveyard target")
        void noEffectWithEmptyGraveyard() {
            castSunTitan();
            harness.passBothPriorities();

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Only permanent cards with MV ≤ 3 are offered — filters out high MV and non-permanents")
        void filtersCorrectly() {
            Card bears = new RuneclawBear();
            Card fog = new Fog();
            Card stoneGolem = new StoneGolem();
            Card plains = new Plains();
            harness.setGraveyard(player1, List.of(bears, fog, stoneGolem, plains));
            castSunTitan();
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).cards())
                    .containsExactly(bears, plains);
            chooseGraveyardTarget(0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();

            harness.assertOnBattlefield(player1, "Runeclaw Bear");
        }
    }

    @Nested
    @DisplayName("Attack trigger")
    @CardUsed({SunTitan.class, RuneclawBear.class, Fog.class, StoneGolem.class, Plains.class, Pacifism.class, WallOfFrost.class})
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Sun Titan triggers may ability prompt")
        void attackTriggersMayPrompt() {
            Card bears = new RuneclawBear();
            harness.setGraveyard(player1, List.of(bears));
            addCreatureReady(player1, new SunTitan());

            declareAttackers(List.of(0));
            chooseGraveyardTarget(0);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting attack may returns the previously targeted card")
        void attackReturnsCard() {
            Card bears = new RuneclawBear();
            harness.setGraveyard(player1, List.of(bears));
            addCreatureReady(player1, new SunTitan());

            declareAttackers(List.of(0));
            chooseGraveyardTarget(0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertOnBattlefield(player1, "Runeclaw Bear");
            harness.assertNotInGraveyard(player1, "Runeclaw Bear");
        }

        @Test
        @DisplayName("Declining attack may ability skips graveyard return")
        void decliningAttackMaySkipsReturn() {
            Card bears = new RuneclawBear();
            harness.setGraveyard(player1, List.of(bears));
            addCreatureReady(player1, new SunTitan());

            declareAttackers(List.of(0));
            chooseGraveyardTarget(0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
            harness.assertInGraveyard(player1, "Runeclaw Bear");
        }
    }

    @Test
    void choosesTargetBeforeEtbAbilityResolves() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));
        castSunTitan();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    void cannotReturnOpponentsGraveyardCard() {
        harness.setGraveyard(player2, List.of(new RuneclawBear()));
        castSunTitan();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    void returnsPermanentWithManaValueExactlyThree() {
        harness.setGraveyard(player1, List.of(new WallOfFrost()));
        castSunTitan();
        harness.passBothPriorities();
        chooseGraveyardTarget(0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Wall of Frost");
        harness.assertNotInGraveyard(player1, "Wall of Frost");
    }

    @Test
    void cannotSwitchToAnotherCardWhenTargetLeavesGraveyard() {
        RuneclawBear target = new RuneclawBear();
        Plains other = new Plains();
        harness.setGraveyard(player1, List.of(target, other));
        castSunTitan();
        harness.passBothPriorities();
        chooseGraveyardTarget(0);
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    void returnedAuraEntersAttachedToChosenCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new Pacifism()));
        castSunTitan();
        harness.passBothPriorities();
        chooseGraveyardTarget(0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, host.getId());
        harness.assertOnBattlefield(player1, "Pacifism");
        assertThat(findPermanent(player1, "Pacifism").getAttachedTo()).isEqualTo(host.getId());
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    private void chooseGraveyardTarget(int index) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).get(index).getId()));
    }

    private void castSunTitan() {
        harness.castFromHand(player1, new SunTitan(), "{4}{W}{W}");
    }
}
