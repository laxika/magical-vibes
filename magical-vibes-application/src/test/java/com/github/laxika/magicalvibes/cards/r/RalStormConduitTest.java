package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RalStormConduit.class, Shock.class, Demolish.class, Mountain.class})
class RalStormConduitTest extends BaseCardTest {

    @Test
    @DisplayName("+2 scries 1 and adds two loyalty")
    void plusTwoScries() {
        Permanent ral = addReadyRal(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("The first ability deals damage when you cast an instant")
    void castInstantTriggersDamage() {
        addReadyRal(player1, 4);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The first ability also triggers for the copy made by -2")
    void copyTriggersDamage() {
        addReadyRal(player1, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.MayAbilityChosen(false));
        }
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Casting a sorcery triggers Ral before the spell resolves")
    void castSorceryTriggersDamage() {
        addReadyRal(player1, 4);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, mountain.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(mountain);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(mountain);
    }

    @Test
    @DisplayName("Ral can target himself with the damage trigger")
    void damageCanTargetOwnPlaneswalker() {
        Permanent ral = addReadyRal(player1, 4);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, ral.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ral does not trigger for an opponent's instant")
    void opponentCastDoesNotTrigger() {
        addReadyRal(player1, 4);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("+2 can put the top card on the bottom of the library")
    void scryCanBottomCard() {
        addReadyRal(player1, 4);
        Mountain top = new Mountain();
        Demolish second = new Demolish();
        harness.setLibrary(player1, List.of(top, second));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
    }

    @Test
    @DisplayName("The delayed copy survives Ral dying and copies only the next spell")
    void delayedCopySurvivesSourceAndIsUsedOnce() {
        Permanent ral = addReadyRal(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ral);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The delayed copy allows a new target independently of the original")
    void delayedCopyCanChooseNewTarget() {
        addReadyRal(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("+2 adds loyalty even when the library is empty")
    void scryWithEmptyLibrary() {
        Permanent ral = addReadyRal(player1, 4);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ral.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting a planeswalker does not trigger Ral")
    void planeswalkerCastDoesNotTrigger() {
        addReadyRal(player1, 4);
        harness.setHand(player1, List.of(new RalStormConduit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An unused delayed copy expires at the end of the turn")
    void delayedCopyExpiresAtEndOfTurn() {
        addReadyRal(player1, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyRal(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new RalStormConduit());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
