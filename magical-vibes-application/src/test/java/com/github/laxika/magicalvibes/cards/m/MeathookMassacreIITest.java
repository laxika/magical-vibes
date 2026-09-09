package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeathookMassacreII.class, GrizzlyBears.class, Shock.class})
class MeathookMassacreIITest extends BaseCardTest {

    @Test
    @DisplayName("On entering, each player sacrifices X creatures")
    void eachPlayerSacrificesXCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castMeathook(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        resolveStackAndDeclineMayAbilities();
    }

    @Test
    @DisplayName("You may pay 3 life to return your dead creature with a finality counter")
    void controllerMayPayToReturnOwnCreature() {
        harness.addToBattlefield(player1, new MeathookMassacreII());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        destroyCreature(player2, bears.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanentByCardId(player1, bears.getCard().getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("An opponent may pay 3 life to keep their dead creature in the graveyard")
    void opponentMayPayToKeepTheirCreature() {
        harness.addToBattlefield(player1, new MeathookMassacreII());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        destroyCreature(player1, bears.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanentByCardId(player1, bears.getCard().getId())).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("If an opponent does not pay, their dead creature returns under your control")
    void opponentDecliningReturnsCreatureUnderYourControl() {
        harness.addToBattlefield(player1, new MeathookMassacreII());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        destroyCreature(player1, bears.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        Permanent returned = findPermanentByCardId(player1, bears.getCard().getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    private void castMeathook(int xValue) {
        harness.setHand(player1, List.of(new MeathookMassacreII()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, xValue * 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gs.playCard(gd, player1, 0, xValue, null, null);
    }

    private void destroyCreature(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent findPermanentByCardId(Player player, UUID cardId) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst().orElse(null);
    }

    private void resolveStackAndDeclineMayAbilities() {
        while (!gd.stack.isEmpty() || gd.interaction.isAwaitingInput()) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice choice) {
                Player chooser = choice.playerId().equals(player1.getId()) ? player1 : player2;
                harness.handleMayAbilityChosen(chooser, false);
            } else if (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            } else {
                break;
            }
        }
    }
}
