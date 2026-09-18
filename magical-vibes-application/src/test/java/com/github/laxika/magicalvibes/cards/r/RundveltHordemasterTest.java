package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RundveltHordemaster.class, RagingGoblin.class, GrizzlyBears.class, Shock.class})
class RundveltHordemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Other Goblins you control get +1/+1")
    void boostsOtherGoblinsYouControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RundveltHordemaster());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentGoblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Goblin death exiles the top card and allows casting a Goblin creature")
    void goblinDeathAllowsCastingExiledGoblin() {
        Card exiledGoblin = new RagingGoblin();
        harness.setLibrary(player1, List.of(exiledGoblin));
        harness.addToBattlefield(player1, new RundveltHordemaster());
        Permanent dyingGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        destroyWithShock(dyingGoblin);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledGoblin);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, exiledGoblin.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Raging Goblin");
    }

    @Test
    @DisplayName("Its own death also exiles the top card")
    void selfDeathTriggersExileAbility() {
        Card exiledGoblin = new RagingGoblin();
        harness.setLibrary(player1, List.of(exiledGoblin));
        Permanent source = harness.addToBattlefieldAndReturn(player1, new RundveltHordemaster());
        destroyWithShock(source);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledGoblin);
    }

    @Test
    @DisplayName("A non-Goblin death does not trigger the exile ability")
    void nonGoblinDeathDoesNotTrigger() {
        Card topCard = new RagingGoblin();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new RundveltHordemaster());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        destroyWithShock(dyingCreature);

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("A non-Goblin card exiled by the trigger cannot be cast")
    void nonGoblinCardCannotBeCast() {
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiledCard));
        harness.addToBattlefield(player1, new RundveltHordemaster());
        Permanent dyingGoblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        destroyWithShock(dyingGoblin);

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiledCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void destroyWithShock(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
