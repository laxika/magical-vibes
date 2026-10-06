package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReckonersBargain.class, GrizzlyBears.class, Spellbook.class, Pacifism.class, NetworkTerminal.class})
class ReckonersBargainTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature, gains life equal to its mana value, and draws two cards")
    void sacrificesCreatureGainsLifeAndDraws() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears firstDraw = new GrizzlyBears();
        Spellbook secondDraw = new Spellbook();
        harness.setHand(player1, List.of(new ReckonersBargain()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can sacrifice an artifact")
    void canSacrificeArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new ReckonersBargain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Spellbook()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithSacrifice(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot sacrifice a non-artifact non-creature permanent")
    void cannotSacrificeNonArtifactNonCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        enchantment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new ReckonersBargain()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature");
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution and its mana value survives leaving the graveyard")
    void remembersSacrificedArtifactManaValue() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        harness.setHand(player1, List.of(new ReckonersBargain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Spellbook()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstantWithSacrifice(player1, 0, null, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Network Terminal");
        harness.assertInGraveyard(player1, "Network Terminal");
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot cast without paying the sacrifice cost even with a suitable permanent available")
    void cannotOmitSacrificeCost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReckonersBargain()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Reckoner's Bargain");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot sacrifice a permanent controlled by the opponent")
    void cannotSacrificeOpponentsPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReckonersBargain()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Reckoner's Bargain");
        harness.assertLife(player1, 20);
    }
}
