package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LarderZombie.class, GrizzlyBears.class, Island.class})
class LarderZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping three untapped creatures surveils 1")
    void tappingThreeCreaturesSurveilsOne() {
        Permanent zombie = addCreatureReady(player1, new LarderZombie());
        Permanent creatureA = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureB = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        int sourceIdx = gd.playerBattlefields.get(player1.getId()).indexOf(zombie);
        harness.activateAbility(player1, sourceIdx, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(zombie.isTapped()).isTrue();
        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining surveil leaves the top card on the library")
    void decliningSurveilLeavesTopCardOnLibrary() {
        addCreatureReady(player1, new LarderZombie());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Cannot activate without three untapped creatures")
    void cannotActivateWithoutThreeCreatures() {
        addCreatureReady(player1, new LarderZombie());
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay the tap cost, including Larder Zombie")
    void summoningSickCreaturesCanPayCost() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LarderZombie());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LarderZombie());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new LarderZombie());
        Card topCard = new LarderZombie();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(source.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped Larder Zombie can activate using three other creatures")
    void tappedSourceCanActivate() {
        Permanent source = addCreatureReady(player1, new LarderZombie());
        source.tap();
        Permanent first = addCreatureReady(player1, new LarderZombie());
        Permanent second = addCreatureReady(player1, new LarderZombie());
        Permanent third = addCreatureReady(player1, new LarderZombie());
        Card topCard = new LarderZombie();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Tapped creatures cannot count toward the three-creature cost")
    void tappedCreatureCannotPayCost() {
        Permanent source = addCreatureReady(player1, new LarderZombie());
        Permanent second = addCreatureReady(player1, new LarderZombie());
        Permanent third = addCreatureReady(player1, new LarderZombie());
        third.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's creatures cannot pay the tap cost")
    void opponentsCreaturesCannotPayCost() {
        Permanent source = addCreatureReady(player1, new LarderZombie());
        Permanent friendly = addCreatureReady(player1, new LarderZombie());
        Permanent opposing = addCreatureReady(player2, new LarderZombie());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(friendly.isTapped()).isFalse();
        assertThat(opposing.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveilling an empty library resolves without a choice or a draw")
    void emptyLibraryResolvesWithoutChoice() {
        Permanent source = addCreatureReady(player1, new LarderZombie());
        addCreatureReady(player1, new LarderZombie());
        addCreatureReady(player1, new LarderZombie());
        harness.setLibrary(player1, List.of());
        List<Card> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
    }

    @Test
    @DisplayName("Untapped lands cannot pay the creature tap cost")
    void noncreaturesCannotPayCost() {
        Permanent source = addCreatureReady(player1, new LarderZombie());
        Permanent second = addCreatureReady(player1, new LarderZombie());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil 1 moves only the top card and leaves the remaining library in order")
    void surveilsOnlyTopCard() {
        addCreatureReady(player1, new LarderZombie());
        addCreatureReady(player1, new LarderZombie());
        addCreatureReady(player1, new LarderZombie());
        Card top = new Island();
        Card second = new LarderZombie();
        Card third = new Island();
        harness.setLibrary(player1, List.of(top, second, third));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
    }
}
