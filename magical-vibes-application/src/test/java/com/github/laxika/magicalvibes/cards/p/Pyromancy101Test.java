package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Pyromancy101.class, GrizzlyBears.class, PullFromEternity.class})
class Pyromancy101Test extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage and teaches the spell to a creature you control")
    void dealsDamageAndTeaches() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        castPyromancy();
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Pyromancy 101");
        assertThat(gs.getEffectiveActivatedAbilities(gd, creature))
                .anyMatch(ability -> ability.getDescription().contains("Copy the exiled card"));
        harness.assertNotInGraveyard(player1, "Pyromancy 101");
    }

    @Test
    @DisplayName("Exiles the spell even when its controller controls no creatures")
    void exilesWithoutCreatureToTeach() {
        castPyromancy();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Pyromancy 101");
        harness.assertNotInGraveyard(player1, "Pyromancy 101");
    }

    @Test
    @DisplayName("The taught ability casts a copy for {1}{R}, while the original remains exiled")
    void castsTaughtCopyForTeachCost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castPyromancy();
        harness.handlePermanentChosen(player1, creature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        int abilityIndex = gs.getEffectiveActivatedAbilities(gd, creature).size() - 1;
        harness.activateAbility(player1, creatureIndex, abilityIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Pyromancy 101");
    }

    @Test
    @DisplayName("The taught ability disappears when the original leaves exile without being cast")
    void losesTaughtAbilityWhenOriginalLeavesExile() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castPyromancy();
        harness.handlePermanentChosen(player1, creature.getId());
        var taughtCard = gd.getPlayerExiledCards(player1.getId()).getFirst();

        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, taughtCard.getId());

        harness.assertInGraveyard(player1, "Pyromancy 101");
        assertThat(gs.getEffectiveActivatedAbilities(gd, creature)).isEmpty();
    }

    @Test
    @DisplayName("Declining the taught copy leaves only the original in exile")
    void declinesTaughtCopy() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castPyromancy();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Pyromancy 101");
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An illegal damage target prevents teaching and puts the spell in the graveyard")
    void illegalTargetPreventsTeaching() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyromancy101()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pyromancy 101");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castPyromancy() {
        harness.setHand(player1, List.of(new Pyromancy101()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
