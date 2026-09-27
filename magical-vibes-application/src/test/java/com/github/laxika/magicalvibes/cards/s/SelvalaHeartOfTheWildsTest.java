package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelvalaHeartOfTheWilds.class, HillGiant.class, Forest.class})
class SelvalaHeartOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("The entering creature's controller may draw when it has uniquely greatest power")
    void enteringCreatureControllerMayDraw() {
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player2, new HillGiant());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("A tied greatest power does not draw")
    void tiedGreatestPowerDoesNotDraw() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player1, new SelvalaHeartOfTheWilds());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new HillGiant());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Adds mana equal to the greatest power in any combination of colors")
    void addsGreatestPowerInAnyCombinationOfColors() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent selvala = harness.addToBattlefieldAndReturn(player1, new SelvalaHeartOfTheWilds());
        selvala.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int selvalaIndex = gd.playerBattlefields.get(player1.getId()).indexOf(selvala);
        harness.activateAbility(player1, selvalaIndex, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
