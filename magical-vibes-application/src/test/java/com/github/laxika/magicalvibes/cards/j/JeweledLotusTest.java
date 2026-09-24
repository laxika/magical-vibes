package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({JeweledLotus.class, GrizzlyBears.class})
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
        GrizzlyBears commander = new GrizzlyBears();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
        gd.activePlayerId = player1.getId();
        gd.priorityPassedBy.clear();

        harness.addToBattlefield(player1, new JeweledLotus());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(commander.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getCommanderOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }
}
