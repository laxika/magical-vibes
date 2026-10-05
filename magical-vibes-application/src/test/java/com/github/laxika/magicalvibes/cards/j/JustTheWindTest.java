package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.g.GhoulcallersAccomplice;
import com.github.laxika.magicalvibes.cards.s.SinisterConcoction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JustTheWind.class, GhoulcallersAccomplice.class, SinisterConcoction.class, Catalog.class})
class JustTheWindTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature to its owner's hand")
    void returnsTargetCreatureToOwnersHand() {
        harness.addToBattlefield(player2, new GhoulcallersAccomplice());
        harness.setHand(player1, List.of(new JustTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID targetId = harness.getPermanentId(player2, "Ghoulcaller's Accomplice");

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Ghoulcaller's Accomplice");
        harness.assertInHand(player2, "Ghoulcaller's Accomplice");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new GhoulcallersAccomplice());
        harness.addToBattlefield(player2, new SinisterConcoction());
        harness.setHand(player1, List.of(new JustTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Sinister Concoction")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Madness casts for {U} after being discarded")
    void madnessCastsForBlueMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GhoulcallersAccomplice());
        JustTheWind justTheWind = new JustTheWind();
        harness.setHand(player2, List.of(new Catalog(), justTheWind));
        harness.setLibrary(player2, List.of(new Catalog(), new Catalog()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ghoulcaller's Accomplice");
        harness.assertInHand(player1, "Ghoulcaller's Accomplice");
        harness.assertInGraveyard(player2, "Just the Wind");
    }

    @Test
    @DisplayName("Can return a creature controlled by the caster")
    void returnsOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GhoulcallersAccomplice());
        harness.setHand(player1, List.of(new JustTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Ghoulcaller's Accomplice");
        harness.assertInHand(player1, "Ghoulcaller's Accomplice");
        harness.assertInGraveyard(player1, "Just the Wind");
    }

    @Test
    @DisplayName("Declining madness moves the discarded card from exile to the graveyard")
    void decliningMadnessPutsCardInGraveyard() {
        JustTheWind card = new JustTheWind();
        harness.setHand(player1, List.of(new Catalog(), card));
        harness.setLibrary(player1, List.of(new Catalog(), new Catalog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Just the Wind");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Just the Wind");
    }

    @Test
    @DisplayName("Does not return a creature again after its target leaves the battlefield")
    void targetLeavingBattlefieldMakesSpellFailToResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhoulcallersAccomplice());
        harness.setHand(player1, List.of(new JustTheWind()));
        harness.setHand(player2, List.of(new JustTheWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghoulcaller's Accomplice");
        harness.assertInHand(player2, "Ghoulcaller's Accomplice");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Just the Wind");
        harness.assertInGraveyard(player2, "Just the Wind");
    }
}
