package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VraskaRelicSeeker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Subtlety.class, GrizzlyBears.class, Island.class, MerfolkTrickster.class,
        VraskaRelicSeeker.class})
class SubtletyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can resolve without a target")
    void entersWithoutTarget() {
        harness.setHand(player1, List.of(new Subtlety()));
        addHardcastMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Subtlety");
    }

    @Test
    @DisplayName("ETB puts a creature spell on the bottom of its owner's library")
    void putsCreatureSpellOnBottom() {
        GrizzlyBears creatureSpell = new GrizzlyBears();
        Card topCard = new Island();
        harness.setHand(player2, List.of(creatureSpell));
        harness.setLibrary(player2, List.of(topCard));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new Subtlety()));
        addHardcastMana(player1);
        harness.castCreature(player1, 0, creatureSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, creatureSpell);
        harness.assertOnBattlefield(player1, "Subtlety");
    }

    @Test
    @DisplayName("ETB puts a planeswalker spell on top of its owner's library")
    void putsPlaneswalkerSpellOnTop() {
        VraskaRelicSeeker planeswalkerSpell = new VraskaRelicSeeker();
        Card topCard = new Island();
        harness.setHand(player2, List.of(planeswalkerSpell));
        harness.setLibrary(player2, List.of(topCard));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.castPlaneswalker(player2, 0);
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new Subtlety()));
        addHardcastMana(player1);
        harness.castCreature(player1, 0, planeswalkerSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(planeswalkerSpell, topCard);
        harness.assertOnBattlefield(player1, "Subtlety");
    }

    @Test
    @DisplayName("Evoke exiles a blue card and sacrifices Subtlety after tucking a creature spell")
    void evokeExilesBlueCardAndSacrificesSelf() {
        GrizzlyBears creatureSpell = new GrizzlyBears();
        MerfolkTrickster blueCard = new MerfolkTrickster();
        harness.setHand(player2, List.of(creatureSpell));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.forceActivePlayer(player1);

        Subtlety subtlety = new Subtlety();
        harness.setHand(player1, List.of(subtlety, blueCard));
        harness.getGameService().playCard(gd, player1, 0, 0, creatureSpell.getId(), null,
                List.of(), List.of(), false, null, null, List.of(), null, List.of(), false, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blueCard);
        harness.assertInGraveyard(player1, "Subtlety");
        harness.assertNotOnBattlefield(player1, "Subtlety");
    }

    @Test
    @DisplayName("Cannot target an instant spell")
    void cannotTargetInstantSpell() {
        Card instantSpell = new Shock();
        harness.setHand(player2, List.of(instantSpell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.forceActivePlayer(player1);

        harness.setHand(player1, List.of(new Subtlety()));
        addHardcastMana(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, instantSpell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker spell");
    }

    private void addHardcastMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
