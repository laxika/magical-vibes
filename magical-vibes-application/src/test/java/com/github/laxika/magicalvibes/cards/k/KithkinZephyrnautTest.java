package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.e.EgoErasure;
import com.github.laxika.magicalvibes.cards.g.GameTrailChangeling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KithkinZephyrnaut.class, BallyrushBanneret.class, ElvishWarrior.class,
        GameTrailChangeling.class, EgoErasure.class})
class KithkinZephyrnautTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new KithkinZephyrnaut());
        harness.setLibrary(player1, List.of(new BallyrushBanneret()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Revealing the shared-type card boosts and grants flying and vigilance")
    void revealBuffsSelf() {
        Permanent zephyrnaut = addCreatureReady(player1, new KithkinZephyrnaut());
        BallyrushBanneret topCard = new BallyrushBanneret();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(zephyrnaut.getPowerModifier()).isEqualTo(2);
        assertThat(zephyrnaut.getToughnessModifier()).isEqualTo(2);
        assertThat(zephyrnaut.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(zephyrnaut.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The Kinship boost wears off at cleanup")
    void buffWearsOffAtEndOfTurn() {
        Permanent zephyrnaut = addCreatureReady(player1, new KithkinZephyrnaut());
        harness.setLibrary(player1, List.of(new BallyrushBanneret()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(zephyrnaut.getPowerModifier()).isZero();
        assertThat(zephyrnaut.getToughnessModifier()).isZero();
        assertThat(zephyrnaut.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(zephyrnaut.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal leaves the creature unbuffed")
    void decliningDoesNothing() {
        Permanent zephyrnaut = addCreatureReady(player1, new KithkinZephyrnaut());
        harness.setLibrary(player1, List.of(new BallyrushBanneret()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(zephyrnaut.getPowerModifier()).isZero();
        assertThat(zephyrnaut.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new KithkinZephyrnaut());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Trigger does nothing with an empty library")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new KithkinZephyrnaut());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A changeling on top of the library can be revealed for kinship")
    void changelingSharesCreatureType() {
        Permanent zephyrnaut = addCreatureReady(player1, new KithkinZephyrnaut());
        GameTrailChangeling topCard = new GameTrailChangeling();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(zephyrnaut.getPowerModifier()).isEqualTo(2);
        assertThat(zephyrnaut.getToughnessModifier()).isEqualTo(2);
        assertThat(zephyrnaut.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(zephyrnaut.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Kinship does not trigger during an opponent's upkeep")
    void noTriggerDuringOpponentsUpkeep() {
        Permanent zephyrnaut = addCreatureReady(player1, new KithkinZephyrnaut());
        harness.setLibrary(player1, List.of(new BallyrushBanneret()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(zephyrnaut.getPowerModifier()).isZero();
        assertThat(zephyrnaut.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(zephyrnaut.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Kinship checks current creature types after Ego Erasure resolves in response")
    void losingCreatureTypesPreventsKinshipReveal() {
        Permanent zephyrnaut = addCreatureReady(player1, new KithkinZephyrnaut());
        harness.setLibrary(player1, List.of(new BallyrushBanneret()));
        harness.setHand(player1, List.of(new EgoErasure()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        assertThat(gqs.shareCreatureType(gd, zephyrnaut, gd.playerDecks.get(player1.getId()).getFirst()))
                .isFalse();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(zephyrnaut.getToughnessModifier()).isZero();
        assertThat(zephyrnaut.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(zephyrnaut.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

}
