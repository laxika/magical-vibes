package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeatedArgument.class, GrizzlyBears.class, AvatarOfMight.class})
class HeatedArgumentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 to creature; declining the exile deals no damage to controller")
    void declineExileNoControllerDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new HeatedArgument()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4); // 4 generic + 1 red

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, false); // decline exile

        harness.assertNotOnBattlefield(player2, "Grizzly Bears"); // 6 kills 2/2
        harness.assertLife(player2, 20); // no controller damage
        // Graveyard card not exiled
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2); // original card + Heated Argument
    }

    @Test
    @DisplayName("Accepting the exile deals 2 to the creature's controller and exiles a graveyard card")
    void acceptExileDamagesControllerAndExiles() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = avatar.getId();
        harness.setHand(player1, List.of(new HeatedArgument()));
        GrizzlyBears gyCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(gyCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, true); // exile a card

        // Avatar (8/8) survives 6 damage
        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(avatar.getMarkedDamage()).isEqualTo(6);
        // Controller takes 2 damage
        harness.assertLife(player2, 18);
        // Graveyard card was exiled
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(gyCard);
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == gyCard);
    }

    @Test
    @DisplayName("Accepting with an empty graveyard deals no controller damage")
    void acceptWithEmptyGraveyardNoDamage() {
        harness.addToBattlefield(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new HeatedArgument()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleMayAbilityChosen(player1, true); // accept, but nothing to exile

        harness.assertLife(player2, 20); // no controller damage
    }

    @Test
    @DisplayName("Controller damage waits until a graveyard card has been chosen and exiled")
    void exileChoicePrecedesControllerDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new HeatedArgument()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        harness.assertLife(player2, 20);

        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.exiledCards).anyMatch(e -> e.card() == second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Lethal creature damage still permits exile and damage to its controller")
    void lethalCreatureDamageStillDamagesController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new HeatedArgument()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
        assertThat(gd.exiledCards).anyMatch(e -> e.card() == graveyardCard);
    }

    @Test
    @DisplayName("An illegal creature target prevents both the exile and controller damage")
    void removedTargetPreventsAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new HeatedArgument()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.exiledCards).noneMatch(e -> e.card() == graveyardCard);
        harness.assertLife(player2, 20);
    }
}
