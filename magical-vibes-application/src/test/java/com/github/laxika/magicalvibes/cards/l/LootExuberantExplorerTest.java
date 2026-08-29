package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LootExuberantExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("Controller may play one additional land each turn; opponents may not")
    void grantsControllerOneExtraLandPlay() {
        harness.addToBattlefield(player1, new LootExuberantExplorer());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability offers only creatures within the number of lands controlled")
    void abilityUsesControlledLandCountAsManaValueCap() {
        harness.addToBattlefield(player1, new LootExuberantExplorer());
        findPermanent(player1, "Loot, Exuberant Explorer").setSummoningSick(false);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        GrizzlyBears bears = new GrizzlyBears();
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(
                bears,
                new HillGiant(),
                elves,
                new Shock(),
                new Forest(),
                new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId(), elves.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardsChosen(List.of(bears.getId())));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }
}
