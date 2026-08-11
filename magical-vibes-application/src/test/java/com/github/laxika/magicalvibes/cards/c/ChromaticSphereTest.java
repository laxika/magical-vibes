package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChromaticSphereTest extends BaseCardTest {

    @Test
    @DisplayName("Activating adds mana, draws a card, and sacrifices itself")
    void activateAddsManaDrawsAndSacrifices() {
        harness.addToBattlefield(player1, new ChromaticSphere());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLibrary(player1, List.of(new Plains()));
        GameData gd = harness.getGameData();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
