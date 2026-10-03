package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.r.RitualOfRejuvenation;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.l.LoomingAltisaur;
import com.github.laxika.magicalvibes.cards.t.TerritorialHammerskull;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BishopOfRebirth.class, RaptorCompanion.class, RitualOfRejuvenation.class, LoomingAltisaur.class, Plains.class, TerritorialHammerskull.class})
class BishopOfRebirthTest extends BaseCardTest {


    @Nested
    @DisplayName("Attack trigger")
    @CardUsed({BishopOfRebirth.class, RaptorCompanion.class, RitualOfRejuvenation.class, LoomingAltisaur.class, Plains.class, TerritorialHammerskull.class})
    class AttackTrigger {

        @Test
        @DisplayName("Attacking requires a graveyard target before the optional return")
        void attackTriggersMayPrompt() {
            harness.setGraveyard(player1, List.of(new RaptorCompanion()));
            addReadyBishop(player1);

            declareAttackers(List.of(0));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId()).isEqualTo(player1.getId());
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        }

        @Test
        @DisplayName("Choosing a target then accepting the optional return puts it on the battlefield")
        void returnsCreatureWithLowManaValue() {
            Card bears = new RaptorCompanion();
            harness.setGraveyard(player1, List.of(bears));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).cards()).containsExactly(bears);
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertOnBattlefield(player1, "Raptor Companion");
            harness.assertNotInGraveyard(player1, "Raptor Companion");
        }

        @Test
        @DisplayName("Cannot return non-creature card (instant) from graveyard")
        void cannotReturnNonCreature() {
            harness.setGraveyard(player1, List.of(new RitualOfRejuvenation()));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Cannot return non-creature permanent (land) from graveyard")
        void cannotReturnNonCreaturePermanent() {
            harness.setGraveyard(player1, List.of(new Plains()));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Cannot return creature with MV > 3 from graveyard")
        void cannotReturnHighManaValueCreature() {
            harness.setGraveyard(player1, List.of(new LoomingAltisaur())); // MV 4
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Declining may ability does not return anything")
        void decliningMaySkipsReturn() {
            Card bears = new RaptorCompanion();
            harness.setGraveyard(player1, List.of(bears));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
            assertThat(gd.stack).isEmpty();
            harness.assertInGraveyard(player1, "Raptor Companion");
        }

        @Test
        @DisplayName("Attack has no ability on the stack if no legal graveyard target exists")
        void noEffectWithEmptyGraveyard() {
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Only creatures with mana value at most three qualify")
        void filtersCorrectly() {
            Card bears = new RaptorCompanion();
            Card instant = new RitualOfRejuvenation();
            Card expensiveCreature = new LoomingAltisaur();
            Card plains = new Plains();
            harness.setGraveyard(player1, List.of(bears, instant, expensiveCreature, plains));
            addReadyBishop(player1);

            declareAttackers(List.of(0));
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

            // Only Raptor Companion should be a valid choice
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).cards()).containsExactly(bears);
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertOnBattlefield(player1, "Raptor Companion");
        }
    }


    @Test
    void returnsManaValueThreeCreatureUntappedAndNotAttacking() {
        Card creature = new TerritorialHammerskull();
        harness.setGraveyard(player1, List.of(creature));
        Permanent bishop = addReadyBishop(player1);

        declareAttackers(List.of(0));
        assertThat(bishop.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creature.getId())).findFirst().orElseThrow();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isAttacking()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    void cannotChooseOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new RaptorCompanion()));
        addReadyBishop(player1);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Raptor Companion");
    }

    @Test
    void removedTargetCannotBeReplacedAtResolution() {
        Card target = new RaptorCompanion();
        Card other = new TerritorialHammerskull();
        harness.setGraveyard(player1, List.of(target, other));
        addReadyBishop(player1);

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Territorial Hammerskull");
        harness.assertNotOnBattlefield(player1, "Territorial Hammerskull");
    }

    private Permanent addReadyBishop(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BishopOfRebirth());
        perm.setSummoningSick(false);
        return perm;
    }
}
