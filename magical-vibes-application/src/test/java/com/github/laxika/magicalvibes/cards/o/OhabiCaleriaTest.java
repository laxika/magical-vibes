package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FemerefArchers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkyhunterSkirmisher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OhabiCaleria.class, GrizzlyBears.class, Forest.class, FemerefArchers.class, SkyhunterSkirmisher.class})
class OhabiCaleriaTest extends BaseCardTest {

    @Test
    @DisplayName("Ohabi untaps your Archers during an opponent's untap step")
    void untapsYourArchersDuringOpponentsUntapStep() {
        Permanent ohabi = addReady(player1, new OhabiCaleria());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        ohabi.tap();
        bears.tap();
        forest.tap();

        harness.performUntapStep(player2);

        assertThat(ohabi.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An Archer dealing damage to a creature may be paid for to draw")
    void archerDamageMayDraw() {
        Permanent ohabi = addReady(player1, new OhabiCaleria());
        Permanent blocker = addReady(player2, new GrizzlyBears());
        ohabi.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An Archer dealing damage to a creature may decline the draw")
    void archerDamageMayDeclineDraw() {
        Permanent ohabi = addReady(player1, new OhabiCaleria());
        Permanent blocker = addReady(player2, new GrizzlyBears());
        ohabi.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, new ArrayList<>());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An Archer dealing noncombat damage to a creature may be paid for to draw")
    void archerNoncombatDamageMayDraw() {
        addReady(player1, new OhabiCaleria());
        Permanent archers = addReady(player1, new FemerefArchers());
        Permanent flyer = addReady(player2, new SkyhunterSkirmisher());
        flyer.setAttacking(true);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));
        harness.setHand(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, flyer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(archers.isTapped()).isTrue();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
