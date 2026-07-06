package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ForumNecroscribeTest extends BaseCardTest {

    

    // ===== Repartee: reanimate a creature from your graveyard =====

    @Test
    @DisplayName("Casting an instant that targets a creature returns a creature card from graveyard to battlefield")
    void reparteeReturnsCreature() {
        harness.addToBattlefield(player1, new ForumNecroscribe());
        harness.addToBattlefield(player1, new HillGiant());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.passBothPriorities(); // resolve return trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new ForumNecroscribe());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    // ===== Ward—Discard a card =====

    @Test
    @DisplayName("Ward triggers when an opponent targets Forum Necroscribe")
    void wardTriggersOnOpponentSpell() {
        Permanent necroscribe = addReadyNecroscribe();

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, necroscribe.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Forum Necroscribe");
    }

    @Test
    @DisplayName("Ward counters the opponent's spell when they have no card to discard")
    void wardCountersWhenNoCards() {
        Permanent necroscribe = addReadyNecroscribe();

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock())); // Shock is the only card
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, necroscribe.getId());
        harness.passBothPriorities(); // resolve Ward trigger — no cards to discard

        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Shock"));
        assertThat(necroscribe.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Ward counters the opponent's spell when they decline to discard")
    void wardCountersWhenDeclined() {
        Permanent necroscribe = addReadyNecroscribe();

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, necroscribe.getId());
        harness.passBothPriorities(); // resolve Ward trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false); // decline to discard

        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(necroscribe.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Opponent may discard a card to prevent the counter")
    void wardPreventedByDiscard() {
        Permanent necroscribe = addReadyNecroscribe();

        beginOpponentTurn();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, necroscribe.getId());
        harness.passBothPriorities(); // resolve Ward trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true); // choose to discard

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0); // discard Grizzly Bears

        // Discard happened (not a counter) — Shock is not countered
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Helpers =====

    private Permanent addReadyNecroscribe() {
        Permanent perm = new Permanent(new ForumNecroscribe());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void beginOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
