package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmissaryOfGrudges.class, Shock.class, GrizzlyBears.class})
class EmissaryOfGrudgesTest extends BaseCardTest {

    @Test
    void retargetsChosenOpponentsSpellThatTargetsYou() {
        Permanent emissary = castEmissary();
        Shock shock = castOpponentShock(player1.getId());

        harness.activateAbility(player1, battlefieldIndex(emissary), null, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void retargetsChosenOpponentsSpellThatTargetsYourPermanent() {
        Permanent emissary = castEmissary();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Shock shock = castOpponentShock(creature.getId());

        harness.activateAbility(player1, battlefieldIndex(emissary), null, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 18);
    }

    @Test
    void rejectsSpellsNotControlledByChosenPlayerOrNotTargetingYou() {
        Permanent emissary = castEmissary();
        Shock ownShock = new Shock();
        harness.setHand(player1, List.of(ownShock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(emissary), null, ownShock.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    void canBeActivatedOnlyOnce() {
        Permanent emissary = castEmissary();
        Shock shock = castOpponentShock(player1.getId());

        harness.activateAbility(player1, battlefieldIndex(emissary), null, shock.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(emissary), null, shock.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent castEmissary() {
        harness.setHand(player1, List.of(new EmissaryOfGrudges()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        return findPermanent(player1, "Emissary of Grudges");
    }

    private Shock castOpponentShock(java.util.UUID targetId) {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, targetId);
        harness.passPriority(player2);
        return shock;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
