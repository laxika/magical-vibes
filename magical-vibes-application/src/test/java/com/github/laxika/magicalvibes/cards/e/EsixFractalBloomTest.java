package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JadeMage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EsixFractalBloom.class, GrizzlyBears.class, JadeMage.class})
class EsixFractalBloomTest extends BaseCardTest {

    @Test
    @DisplayName("The first token creation each turn may copy any other creature")
    void createsCopiesOfChosenCreature() {
        Permanent esix = harness.addToBattlefieldAndReturn(player1, new EsixFractalBloom());
        Permanent jadeMage = harness.addToBattlefieldAndReturn(player1, new JadeMage());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addJadeMageMana();

        harness.activateAbility(player1, 1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals(opponentCreature.getCard().getName())
                        && permanent.getCard().getPower() == opponentCreature.getCard().getPower()
                        && permanent.getCard().getToughness() == opponentCreature.getCard().getToughness());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jadeMage, esix);
    }

    @Test
    @DisplayName("Declining Esix creates the original tokens and consumes the replacement for the turn")
    void declineCreatesOriginalTokensOnlyOnce() {
        harness.addToBattlefield(player1, new EsixFractalBloom());
        harness.addToBattlefield(player1, new JadeMage());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addJadeMageMana();

        harness.activateAbility(player1, 1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.activateAbility(player1, 1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Saproling"))
                .hasSize(2);
    }

    private void addJadeMageMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
