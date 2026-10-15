package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MerfolkTrickster;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VraskaRelicSeeker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
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
        VraskaRelicSeeker.class, Shock.class})
class SubtletyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can resolve without a target")
    void entersWithoutTarget() {
        harness.castFromHand(player1, new Subtlety(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Subtlety");
    }

    @Test
    @DisplayName("ETB puts a creature spell on the bottom of its owner's library")
    void putsCreatureSpellOnBottom() {
        GrizzlyBears creatureSpell = new GrizzlyBears();
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, creatureSpell, "{1}{G}");
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
        harness.setLibrary(player2, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, planeswalkerSpell, "{4}{B}{G}");
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
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, creatureSpell, "{1}{G}");
        harness.forceActivePlayer(player1);

        Subtlety subtlety = new Subtlety();
        harness.setHand(player1, List.of(subtlety, blueCard));
        harness.castInstantWithAlternateExileFromHand(player1, 0, creatureSpell.getId(), 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1: Subtlety - sacrifice this creature");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Top");
        harness.passBothPriorities();

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

    @Test
    @DisplayName("Evoke can exile another Subtlety without choosing a target")
    void evokesWithoutTarget() {
        Subtlety blueCard = new Subtlety();
        harness.setHand(player1, List.of(new Subtlety(), blueCard));

        harness.castInstantWithAlternateExileFromHand(player1, 0, (java.util.UUID) null, 1);
        harness.passBothPriorities();
        PendingInteraction.ColorChoice order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        if (order != null && order.context() instanceof ChoiceContext.SpellCastTriggerOrder) {
            harness.handleListChoice(player1, "1: Subtlety - sacrifice this creature");
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(blueCard);
        harness.assertInGraveyard(player1, "Subtlety");
        harness.assertNotOnBattlefield(player1, "Subtlety");
    }

    @Test
    @DisplayName("Cannot exile a colorless land to pay the evoke cost")
    void cannotExileIslandForEvoke() {
        harness.setHand(player1, List.of(new Subtlety(), new Island()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, (java.util.UUID) null, 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Subtlety");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Subtlety cannot exile itself to pay its own evoke cost")
    void cannotExileItselfForEvoke() {
        harness.setHand(player1, List.of(new Subtlety()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, (java.util.UUID) null, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Subtlety");
    }

    @Test
    @DisplayName("Cannot target a creature already on the battlefield")
    void cannotTargetCreaturePermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Subtlety()));
        addHardcastMana(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Subtlety can put its controller's own creature spell on the bottom")
    void canTargetOwnCreatureSpell() {
        GrizzlyBears creatureSpell = new GrizzlyBears();
        Island topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, creatureSpell, "{1}{G}");
        harness.setHand(player1, List.of(new Subtlety()));
        addHardcastMana(player1);

        harness.castCreature(player1, 0, creatureSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, creatureSpell);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Subtlety");
    }

    @Test
    @DisplayName("The tuck trigger still resolves when the evoke sacrifice resolves first")
    void sacrificeCanResolveBeforeTuck() {
        GrizzlyBears creatureSpell = new GrizzlyBears();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, creatureSpell, "{1}{G}");
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Subtlety(), new Subtlety()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, creatureSpell.getId(), 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2: Subtlety's ETB ability");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Subtlety");
        harness.assertNotOnBattlefield(player1, "Subtlety");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creatureSpell);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private void addHardcastMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
