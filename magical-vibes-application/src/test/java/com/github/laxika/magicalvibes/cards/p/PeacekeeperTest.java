package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Peacekeeper.class, RedwoodTreefolk.class})
class PeacekeeperTest extends BaseCardTest {

    private Permanent addReadyTreefolk(Player player) {
        return addCreatureReady(player, new RedwoodTreefolk());
    }

    @Test
    @DisplayName("Paying {1}{W} during your upkeep keeps Peacekeeper")
    void payingKeepsIt() {
        Permanent peacekeeper = harness.addToBattlefieldAndReturn(player1, new Peacekeeper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(peacekeeper);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the payment sacrifices Peacekeeper")
    void decliningSacrifices() {
        Permanent peacekeeper = harness.addToBattlefieldAndReturn(player1, new Peacekeeper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(peacekeeper);
    }

    @Test
    @DisplayName("An old upkeep trigger does not sacrifice a replacement Peacekeeper")
    void oldTriggerDoesNotSacrificeReplacement() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new Peacekeeper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent replacement = harness.addToBattlefieldAndReturn(player1, new Peacekeeper());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(replacement);
    }

    @Test
    @DisplayName("Peacekeeper does not trigger during upkeep after losing all abilities")
    void losingAllAbilitiesDisablesUpkeepTrigger() {
        Permanent peacekeeper = harness.addToBattlefieldAndReturn(player1, new Peacekeeper());
        peacekeeper.setLosesAllAbilitiesUntilEndOfTurn(true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(peacekeeper);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting without enough mana sacrifices Peacekeeper")
    void notEnoughManaSacrifices() {
        Permanent peacekeeper = harness.addToBattlefieldAndReturn(player1, new Peacekeeper());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(peacekeeper);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        Permanent peacekeeper = harness.addToBattlefieldAndReturn(player1, new Peacekeeper());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(peacekeeper);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Your creatures can't attack while Peacekeeper is on the battlefield")
    void controllerCreaturesCantAttack() {
        harness.addToBattlefield(player1, new Peacekeeper());
        Permanent treefolk = addReadyTreefolk(player1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(treefolk);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The opponent's creatures can't attack either")
    void opponentCreaturesCantAttack() {
        harness.addToBattlefield(player1, new Peacekeeper());
        Permanent treefolk = addReadyTreefolk(player2);

        int index = gd.playerBattlefields.get(player2.getId()).indexOf(treefolk);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures can attack again once Peacekeeper leaves the battlefield")
    void restrictionLiftsWhenPeacekeeperLeaves() {
        Permanent peacekeeper = harness.addToBattlefieldAndReturn(player1, new Peacekeeper());
        Permanent treefolk = addReadyTreefolk(player1);

        gd.playerBattlefields.get(player1.getId()).remove(peacekeeper);

        harness.setLife(player2, 20);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(treefolk);
        declareAttackers(player1, List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
