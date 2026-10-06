package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReitoSentinel.class, NetworkTerminal.class})
class ReitoSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards from the chosen player's library")
    void etbMillsTargetPlayer() {
        harness.setLibrary(player2, List.of(
                new NetworkTerminal(), new NetworkTerminal(), new NetworkTerminal(), new NetworkTerminal()));
        harness.setHand(player1, List.of(new ReitoSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Puts a target card from any graveyard on the bottom of its owner's library")
    void putsTargetGraveyardCardOnOwnerLibraryBottom() {
        Card target = new ReitoSentinel();
        Card existingTop = new NetworkTerminal();
        Card existingBottom = new NetworkTerminal();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existingTop, existingBottom));
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new ReitoSentinel());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, existingBottom, target);
    }

    @Test
    @DisplayName("Rejects a target that is not a card in a graveyard")
    void rejectsNonGraveyardTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ReitoSentinel());
        harness.addToBattlefield(player1, new ReitoSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getCard().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB can target its controller and mills only the available cards")
    void etbCanMillControllerWithShortLibrary() {
        Card first = new NetworkTerminal();
        Card second = new ReitoSentinel();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new ReitoSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Sentinel can activate repeatedly for its own graveyard")
    void canActivateRepeatedlyWhileTapped() {
        Card first = new NetworkTerminal();
        Card second = new ReitoSentinel();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of());
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new ReitoSentinel());
        sentinel.setTapped(true);
        sentinel.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, first.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player1, 0, null, second.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(sentinel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability does nothing if its target leaves the graveyard before resolution")
    void targetLeavingGraveyardMakesAbilityFizzle() {
        Card target = new NetworkTerminal();
        Card existing = new NetworkTerminal();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existing));
        harness.addToBattlefield(player1, new ReitoSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);

        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existing);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activation requires all three mana")
    void cannotActivateWithoutEnoughMana() {
        Card target = new NetworkTerminal();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new ReitoSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }
}
