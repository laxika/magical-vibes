package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AetherMeltdown;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmugglersCopter.class, SageOfShailasClaim.class, Forest.class, AetherMeltdown.class})
class SmugglersCopterTest extends BaseCardTest {

    @Test
    void attackingOffersOptionalDrawThenDiscard() {
        setUpLootCards();
        Permanent copter = addCopterReady(player1);
        addCreatureReady(player1, new SageOfShailasClaim());
        crew(copter);

        declareAttackers(player1, List.of(0));
        resolveLootChoice();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void blockingOffersOptionalDrawThenDiscard() {
        setUpLootCards();
        Permanent copter = addCopterReady(player1);
        addCreatureReady(player1, new SageOfShailasClaim());
        crew(copter);

        addCreatureReady(player2, new SageOfShailasClaim());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveLootChoice();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void decliningLootDoesNotDrawOrDiscard() {
        setUpLootCards();
        Permanent copter = addCopterReady(player1);
        addCreatureReady(player1, new SageOfShailasClaim());
        crew(copter);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void crewAnimatesCopterAndTapsCreature() {
        Permanent copter = addCopterReady(player1);
        Permanent creature = addCreatureReady(player1, new SageOfShailasClaim());

        crew(copter);

        assertThat(gqs.isCreature(gd, copter)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void emptyHandCanLootAndDiscardTheDrawnCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        Permanent copter = addCopterReady(player1);
        addCreatureReady(player1, new SageOfShailasClaim());
        crew(copter);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class) != null) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void summoningSickCreatureCanCrew() {
        Permanent copter = addCopterReady(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SageOfShailasClaim());
        creature.setSummoningSick(true);

        crew(copter);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, copter)).isTrue();
    }

    @Test
    void crewAnimationEndsAtEndOfTurn() {
        Permanent copter = addCopterReady(player1);
        addCreatureReady(player1, new SageOfShailasClaim());
        crew(copter);
        assertThat(gqs.isCreature(gd, copter)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, copter)).isFalse();
    }

    @Test
    void negativePowerCrewMemberReducesCombinedPower() {
        addCopterReady(player1);
        Permanent positive = addCreatureReady(player1, new SageOfShailasClaim());
        Permanent negative = addCreatureReady(player1, new SageOfShailasClaim());
        Permanent extra = addCreatureReady(player1, new SageOfShailasClaim());
        addCreatureReady(player1, new SageOfShailasClaim());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AetherMeltdown());
        aura.setAttachedTo(negative.getId());
        assertThat(gqs.getEffectivePower(gd, negative)).isEqualTo(-2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, positive.getId());
        harness.handlePermanentChosen(player1, negative.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(extra.getId())
                .doesNotContain(player1.getId());
    }

    private void setUpLootCards() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new SageOfShailasClaim()));
    }

    private Permanent addCopterReady(Player player) {
        return addCreatureReady(player, new SmugglersCopter());
    }

    private void crew(Permanent copter) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(copter), null, null);
        harness.passBothPriorities();
    }

    private void resolveLootChoice() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
    }
}
