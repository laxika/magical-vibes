package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MartyrsCry.class, GrizzlyBears.class, Island.class, SavannahLions.class})
class MartyrsCryTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all white creatures and each affected controller draws for their exiled creatures")
    void exilesWhiteCreaturesAndDrawsForEachController() {
        harness.addToBattlefield(player1, new SavannahLions());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SavannahLions());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player1, List.of(new MartyrsCry()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Savannah Lions");
        harness.assertNotOnBattlefield(player2, "Savannah Lions");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
        harness.assertInHand(player2, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Savannah Lions"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Savannah Lions"));
    }
}
