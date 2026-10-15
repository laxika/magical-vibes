package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KuldothaPhoenix.class, IronMyr.class, GoldMyr.class, CopperMyr.class})
class KuldothaPhoenixTest extends BaseCardTest {

    @Test
    void doesNotAutomaticallyTriggerDuringUpkeep() {
        addMetalcraft();
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Kuldotha Phoenix");
    }

    @Test
    void doesNotAutomaticallyTriggerWithoutMetalcraft() {
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentUpkeep() {
        addMetalcraft();
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void payingActivationCostReturnsOnlyTheActivatedPhoenix() {
        KuldothaPhoenix phoenix = new KuldothaPhoenix();
        KuldothaPhoenix otherPhoenix = new KuldothaPhoenix();
        addMetalcraft();
        harness.setGraveyard(player1, List.of(phoenix, otherPhoenix));
        prepareOwnUpkeep(4);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(phoenix, otherPhoenix);
        harness.assertNotOnBattlefield(player1, "Kuldotha Phoenix");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherPhoenix);
        assertThat(findPermanent(player1, "Kuldotha Phoenix").getCard().getId()).isEqualTo(phoenix.getId());
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void choosingNotToActivateKeepsPhoenixInGraveyard() {
        addMetalcraft();
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));
        prepareOwnUpkeep(4);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kuldotha Phoenix");
        harness.assertNotOnBattlefield(player1, "Kuldotha Phoenix");
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotActivateWithoutEnoughMana() {
        addMetalcraft();
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));
        prepareOwnUpkeep(3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Kuldotha Phoenix");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void cannotActivateWithOnlyTwoArtifactsEvenIfOpponentControlsThird() {
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player2, new CopperMyr());
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));
        prepareOwnUpkeep(4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Kuldotha Phoenix");
    }

    @Test
    void cannotActivateDuringOwnMainPhase() {
        addMetalcraft();
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));
        prepareOwnUpkeep(4);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentUpkeep() {
        addMetalcraft();
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));
        prepareOwnUpkeep(4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingMetalcraftAfterActivationDoesNotPreventReturn() {
        addMetalcraft();
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));
        prepareOwnUpkeep(4);
        harness.activateGraveyardAbility(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Copper Myr"));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kuldotha Phoenix");
        harness.assertNotInGraveyard(player1, "Kuldotha Phoenix");
    }

    @Test
    void canActivateAfterPhoenixEntersGraveyardDuringUpkeep() {
        addMetalcraft();
        prepareOwnUpkeep(4);
        harness.setGraveyard(player1, List.of(new KuldothaPhoenix()));

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kuldotha Phoenix");
        harness.assertNotInGraveyard(player1, "Kuldotha Phoenix");
    }

    private void addMetalcraft() {
        harness.addToBattlefield(player1, new IronMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new CopperMyr());
    }

    private void prepareOwnUpkeep(int mana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, mana);
    }
}
