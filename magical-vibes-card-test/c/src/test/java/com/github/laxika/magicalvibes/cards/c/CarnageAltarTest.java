package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CarnageAltar.class, StoneworkPuma.class, Forest.class})
class CarnageAltarTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices a creature and puts the ability on the stack")
    void activatingSacrificesCreature() {
        harness.addToBattlefield(player1, new CarnageAltar());
        harness.addToBattlefield(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Stonework Puma");
        harness.assertInGraveyard(player1, "Stonework Puma");
        harness.assertOnBattlefield(player1, "Carnage Altar");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Carnage Altar");
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving the ability draws a card")
    void resolvingDrawsACard() {
        harness.addToBattlefield(player1, new CarnageAltar());
        harness.addToBattlefield(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new CarnageAltar());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new CarnageAltar());
        harness.addToBattlefield(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new CarnageAltar());
        harness.addToBattlefield(player2, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Stonework Puma");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped altar can sacrifice a tapped creature that entered this turn")
    void canActivateWithTappedPermanents() {
        harness.addToBattlefield(player1, new CarnageAltar());
        harness.addToBattlefield(player1, new StoneworkPuma());
        findPermanent(player1, "Carnage Altar").tap();
        findPermanent(player1, "Stonework Puma").tap();
        findPermanent(player1, "Stonework Puma").setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Stonework Puma");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
        assertThat(findPermanent(player1, "Carnage Altar").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Altar can activate twice in one turn by paying each cost")
    void canActivateRepeatedly() {
        harness.addToBattlefield(player1, new CarnageAltar());
        harness.addToBattlefield(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new StoneworkPuma());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Stonework Puma", "Stonework Puma");
        harness.assertOnBattlefield(player1, "Carnage Altar");
        assertThat(findPermanent(player1, "Carnage Altar").isTapped()).isFalse();
    }
}