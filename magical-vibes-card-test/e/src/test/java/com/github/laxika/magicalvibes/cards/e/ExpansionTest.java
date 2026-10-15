package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.d.DirectCurrent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Expansion.class, AngelsMercy.class, DirectCurrent.class, GrizzlyBears.class, LavaAxe.class})
class ExpansionTest extends BaseCardTest {

    @Test
    @DisplayName("Expansion copies an instant or sorcery spell with mana value 4 or less")
    void expansionCopiesSmallInstantOrSorcery() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy, new Expansion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0, 0, mercy.getId());
        harness.passBothPriorities();

        StackEntry copy = gd.stack.getLast();
        assertThat(copy.getDescription()).isEqualTo("Copy of Angel's Mercy");
        assertThat(copy.isCopy()).isTrue();
    }

    @Test
    @DisplayName("Expansion cannot target an instant or sorcery spell with mana value greater than 4")
    void expansionRejectsLargeInstantOrSorcery() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe, new Expansion()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, lavaAxe.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Explosion deals X damage to one target and makes another player draw X cards")
    void explosionUsesBothTargetsAndPaidX() {
        harness.setHand(player1, List.of(new Expansion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        gs.playModalXCard(gd, player1, 0, 1, 3, null, List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1 + 3);
    }

    @Test
    @DisplayName("Explosion requires a player for its card-draw target")
    void explosionCannotUsePermanentForCardDrawTarget() {
        harness.setHand(player1, List.of(new Expansion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> gs.playModalXCard(
                gd, player1, 0, 1, 3, null, List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void expansionCopyAndOriginalBothResolve() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy, new Expansion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0, 0, mercy.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 27);
        harness.passBothPriorities();
        harness.assertLife(player1, 34);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void expansionCanChooseNewTargetsForSorceryCopy() {
        DirectCurrent current = new DirectCurrent();
        harness.setHand(player1, List.of(current, new Expansion()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castInstant(player1, 0, 0, current.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void expansionCanKeepOriginalTargetsForSorceryCopy() {
        DirectCurrent current = new DirectCurrent();
        harness.setHand(player1, List.of(current, new Expansion()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castInstant(player1, 0, 0, current.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    @Test
    void explosionCanDamageAndDrawForSamePlayer() {
        harness.setHand(player1, List.of(new Expansion()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Expansion(), new Expansion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playModalXCard(gd, player1, 0, 1, 2, null, List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void explosionWithZeroXDoesNotDamageOrDraw() {
        harness.setHand(player1, List.of(new Expansion()));
        harness.setLibrary(player1, List.of(new Expansion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playModalXCard(gd, player1, 0, 1, 0, null, List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void explosionStillDrawsWhenDamageTargetLeavesBattlefield() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Expansion()));
        harness.setLibrary(player1, List.of(new Expansion(), new Expansion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playModalXCard(gd, player1, 0, 1, 2, null, List.of(creature.getId(), player1.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void expansionCanCopyZeroXExplosionUsingChosenFaceManaValue() {
        Expansion explosion = new Expansion();
        harness.setHand(player1, List.of(explosion, new Expansion()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        gs.playModalXCard(gd, player1, 0, 1, 0, null, List.of(player2.getId(), player1.getId()));
        harness.castInstant(player1, 0, 0, explosion.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void expansionRejectsExplosionWithPositiveX() {
        Expansion explosion = new Expansion();
        harness.setHand(player1, List.of(explosion, new Expansion()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.playModalXCard(gd, player1, 0, 1, 1, null, List.of(player2.getId(), player1.getId()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, explosion.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void expansionCannotCopyCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new Expansion()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void explosionCanKillCreatureAndDrawCards() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Expansion()));
        harness.setLibrary(player1, List.of(new Expansion(), new Expansion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playModalXCard(gd, player1, 0, 1, 2, null, List.of(creature.getId(), player1.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void expansionCanCopyOpponentsSpellUsingRedMana() {
        DirectCurrent current = new DirectCurrent();
        harness.setHand(player1, List.of(current));
        harness.setHand(player2, List.of(new Expansion()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castInstant(player2, 0, 0, current.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }
}
