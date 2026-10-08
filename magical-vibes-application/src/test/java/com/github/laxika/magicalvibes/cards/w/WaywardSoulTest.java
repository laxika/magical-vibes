package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaywardSoul.class})
class WaywardSoulTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {U} puts Wayward Soul on top of its owner's library")
    void activatePutsOnTopOfLibrary() {
        harness.addToBattlefield(player1, new WaywardSoul());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wayward Soul");
        harness.assertNotInHand(player1, "Wayward Soul");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(WaywardSoul.class);
    }

    @Test
    @DisplayName("Ability cannot be activated without paying {U}")
    void requiresMana() {
        harness.addToBattlefield(player1, new WaywardSoul());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot be activated with non-blue mana")
    void requiresBlueMana() {
        harness.addToBattlefield(player1, new WaywardSoul());

        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A controlled Wayward Soul returns to its owner's library, preserving library order")
    void returnsToOwnersLibrary() {
        WaywardSoul soul = new WaywardSoul();
        soul.setOwnerId(player2.getId());
        WaywardSoul top = new WaywardSoul();
        WaywardSoul bottom = new WaywardSoul();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(top, bottom));
        harness.addToBattlefield(player1, soul);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wayward Soul");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(soul, top, bottom);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Wayward Soul can activate its ability")
    void activatesWhileTappedAndSummoningSick() {
        WaywardSoul soul = new WaywardSoul();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, soul);
        permanent.setSummoningSick(true);
        permanent.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wayward Soul");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(soul);
    }

    @Test
    @DisplayName("Repeated activations move only the source once and leave another Wayward Soul alone")
    void repeatedActivationsDoNotMoveAnotherPermanent() {
        WaywardSoul source = new WaywardSoul();
        WaywardSoul other = new WaywardSoul();
        harness.setLibrary(player1, List.of());
        harness.addToBattlefield(player1, source);
        harness.addToBattlefield(player1, other);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(source);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(other);
    }
}
