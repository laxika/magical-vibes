package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.r.RishkarPeemaRenegade;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeweledLotus.class, RishkarPeemaRenegade.class})
class JeweledLotusTest extends BaseCardTest {

    @Test
    void addsThreeCommanderOnlyManaOfChosenColor() {
        harness.addToBattlefield(player1, new JeweledLotus());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getCommanderOnlyMana(ManaColor.GREEN)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Jeweled Lotus");
    }

    @Test
    void commanderOnlyManaPaysForCommanderButNotAnOrdinarySpell() {
        RishkarPeemaRenegade commander = new RishkarPeemaRenegade();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();

        harness.addToBattlefield(player1, new JeweledLotus());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.setHand(player1, List.of(new RishkarPeemaRenegade()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(commander.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getCommanderOnlyMana(ManaColor.GREEN)).isZero();
    }

    @Test
    void paysGenericCommanderCostFromHandWithManaOutsideItsColorIdentity() {
        RishkarPeemaRenegade commander = new RishkarPeemaRenegade();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        harness.setHand(player1, List.of(commander));
        harness.addToBattlefield(player1, new JeweledLotus());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(commander.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getCommanderOnlyMana(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void tappedLotusCannotActivateOrBeSacrificedForMana() {
        harness.addToBattlefieldAndReturn(player1, new JeweledLotus()).tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Jeweled Lotus");
        harness.assertNotInGraveyard(player1, "Jeweled Lotus");
        assertThat(gd.playerManaPools.get(player1.getId()).getCommanderOnlyManaTotal()).isZero();
    }
}
