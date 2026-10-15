package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Geistblast.class, CounselOfTheSoratami.class, GrizzlyBears.class})
class GeistblastTest extends BaseCardTest {

    @Test
    @DisplayName("Geistblast deals 2 damage to any target")
    void dealsDamageToAnyTarget() {
        harness.setHand(player1, List.of(new Geistblast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Graveyard ability exiles Geistblast and copies an instant or sorcery spell you control")
    void graveyardAbilityCopiesOwnSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.setGraveyard(player1, List.of(new Geistblast()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.activateGraveyardAbility(player1, 0, counsel.getId());

        harness.assertNotInGraveyard(player1, "Geistblast");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Geistblast"));

        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Graveyard ability cannot target a creature spell")
    void graveyardAbilityCannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.setGraveyard(player1, List.of(new Geistblast()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyCanKeepOriginalTarget() {
        Geistblast original = new Geistblast();
        harness.setHand(player1, List.of(original));
        harness.setGraveyard(player1, List.of(new Geistblast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateGraveyardAbility(player1, 0, original.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Geistblast");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(original);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    void copyCanChooseNewTargetWithoutChangingOriginal() {
        Geistblast original = new Geistblast();
        harness.setHand(player1, List.of(original));
        harness.setGraveyard(player1, List.of(new Geistblast()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateGraveyardAbility(player1, 0, original.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardAbilityCannotCopyOpponentsSpell() {
        Geistblast opposingSpell = new Geistblast();
        harness.setHand(player2, List.of(opposingSpell));
        harness.setGraveyard(player1, List.of(new Geistblast()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, opposingSpell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Geistblast");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void graveyardAbilityRequiresBlueManaAndDoesNotExileOnFailedPayment() {
        Geistblast original = new Geistblast();
        harness.setHand(player1, List.of(original));
        harness.setGraveyard(player1, List.of(new Geistblast()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, original.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Geistblast");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void dealsLethalDamageToCreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Geistblast()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void copiedNontargetedSorceryResolvesWithoutRetargetPrompt() {
        CounselOfTheSoratami original = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(original));
        harness.setGraveyard(player1, List.of(new Geistblast()));
        harness.setLibrary(player1, List.of(
                new Geistblast(), new Geistblast(), new Geistblast(), new Geistblast()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.activateGraveyardAbility(player1, 0, original.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(original);
        assertThat(gd.stack).isEmpty();
    }
}
