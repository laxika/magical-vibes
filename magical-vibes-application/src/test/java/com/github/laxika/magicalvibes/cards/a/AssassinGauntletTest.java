package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AssassinGauntlet.class, Forest.class, GrizzlyBears.class})
class AssassinGauntletTest extends BaseCardTest {

    @Test
    void entersAttachedAndTapsTargetOpponentsCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentCreature2 = addCreatureReady(player2, new GrizzlyBears());
        castGauntlet(creature.getId(), player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent gauntlet = findPermanent(player1, "Assassin Gauntlet");
        assertThat(gauntlet.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature2.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void canEnterWithoutAnAttachedCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        castGauntlet(null, player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent gauntlet = findPermanent(player1, "Assassin Gauntlet");
        assertThat(gauntlet.getAttachedTo()).isNull();
        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    void equipsAndGrantsLootAbilityToEquippedCreature() {
        Permanent gauntlet = addPermanentReady(player1, new AssassinGauntlet());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int gauntletIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gauntlet);
        harness.activateAbility(player1, gauntletIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gauntlet.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void equippedCreatureDrawsThenDiscardsWhenItDealsCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent gauntlet = addPermanentReady(player1, new AssassinGauntlet());
        gauntlet.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));

        creature.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
    }

    private void castGauntlet(java.util.UUID creatureTarget, java.util.UUID opponentTarget) {
        harness.setHand(player1, List.of(new AssassinGauntlet()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        List<java.util.UUID> targetIds = creatureTarget == null
                ? List.of(opponentTarget)
                : List.of(opponentTarget, creatureTarget);
        gs.playCard(gd, player1, 0, 0, null, null, targetIds, List.of());
    }

    private Permanent addPermanentReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
