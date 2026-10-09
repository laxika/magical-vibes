package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DemonicDread.class, GrizzlyBears.class, Mountain.class})
class DemonicDreadTest extends BaseCardTest {


    @Test
    @DisplayName("Target creature can't block this turn")
    void targetCreatureCantBlock() {
        prepareCaster();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, List.of(blocker.getId()));
        harness.passBothPriorities(); // resolve cascade (empty library, no-op)
        harness.passBothPriorities(); // resolve the spell

        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Targeted creature actually cannot be declared as a blocker")
    void targetedCreatureCannotBlock() {
        prepareCaster();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, List.of(blocker.getId()));
        harness.passBothPriorities(); // resolve cascade (empty library, no-op)
        harness.passBothPriorities(); // resolve the spell

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Cascade digs past a land to the first lesser-mana-value nonland")
    void cascadeOffersLesserManaValueNonland() {
        prepareCaster();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        // Demonic Dread is {1}{B}{R} = mana value 3. Dig skips the Mountain and stops at
        // Grizzly Bears (MV 2 < 3).
        harness.setLibrary(player1, List.of(new Mountain(), new GrizzlyBears()));

        harness.castSorcery(player1, 0, List.of(blocker.getId()));
        harness.passBothPriorities(); // resolve the cascade trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");
    }


    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        prepareCaster();
        addCreatureReady(player2, new GrizzlyBears()); // valid target so the spell is playable

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    void cascadeExilesEachRevealedCard() {
        prepareCaster();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Mountain land = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, hit));

        harness.castSorcery(player1, 0, blocker.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .containsExactlyInAnyOrder(land.getId(), hit.getId());
        assertThat(gd.cardsExiledThisTurn).isEqualTo(2);
    }

    @Test
    void cascadeSkipsEqualManaValueAndDeclinedHitGoesToBottom() {
        prepareCaster();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        DemonicDread equal = new DemonicDread();
        GrizzlyBears hit = new GrizzlyBears();
        Mountain below = new Mountain();
        harness.setLibrary(player1, List.of(equal, hit, below));

        harness.castSorcery(player1, 0, blocker.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(below);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(below, equal, hit);
        resolveAllTriggers();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cascadeCastsHitForFreeBeforeDreadResolves() {
        prepareCaster();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Mountain land = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        Mountain below = new Mountain();
        harness.setLibrary(player1, List.of(land, hit, below));

        harness.castSorcery(player1, 0, blocker.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below, land);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playersWhoPlayedCardFromExileThisTurn).contains(player1.getId());
        assertThat(blocker.isCantBlockThisTurn()).isFalse();
        resolveAllTriggers();
        assertThat(blocker.isCantBlockThisTurn()).isTrue();
        harness.assertInGraveyard(player1, "Demonic Dread");
    }

    @Test
    void cannotCastWithoutCreatureTarget() {
        prepareCaster();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Demonic Dread");
    }

    @Test
    void canTargetOwnCreatureAndRestrictionExpiresAfterTurn() {
        prepareCaster();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain()));

        harness.castSorcery(player1, 0, target.getId());
        resolveAllTriggers();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

    private void prepareCaster() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        // Empty library by default so the cascade trigger is a no-op unless a test stocks it.
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new DemonicDread()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
