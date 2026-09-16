package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IxidorsWill.class, InformationDealer.class, ElvishWarrior.class})
class IxidorsWillTest extends BaseCardTest {

    @Test
    void spellResolvesWithoutWizardsOnTheBattlefield() {
        castTargetSpell(2);

        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Warrior");
    }

    @Test
    void countersSpellWhenOneWizardMakesTaxUnaffordable() {
        harness.addToBattlefield(player1, new InformationDealer());
        castTargetSpell(2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void countersSpellWhenControllerDeclinesAffordableWizardTax() {
        harness.addToBattlefield(player1, new InformationDealer());
        castTargetSpell(4);

        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void wizardCountIsEvaluatedWhenCounterSpellResolves() {
        castTargetSpell(2);
        harness.addToBattlefield(player2, new InformationDealer());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void taxScalesToFourForTwoWizards() {
        harness.addToBattlefield(player1, new InformationDealer());
        harness.addToBattlefield(player2, new InformationDealer());
        castTargetSpell(6);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Warrior");
    }

    private void castTargetSpell(int mana) {
        ElvishWarrior warrior = new ElvishWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, mana - 2);

        harness.setHand(player2, List.of(new IxidorsWill()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warrior.getId());
    }
}
