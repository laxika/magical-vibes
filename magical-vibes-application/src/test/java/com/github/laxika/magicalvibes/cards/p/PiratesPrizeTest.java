package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PiratesPrize.class})
class PiratesPrizeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as a sorcery spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PiratesPrize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }


    @Test
    @DisplayName("Resolving draws two cards and creates a Treasure token")
    void resolvingDrawsTwoAndCreatesTreasure() {
        harness.setHand(player1, List.of(new PiratesPrize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        // Drew two cards (hand was 0 after casting the only card, now should be 2)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Treasure created
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure).isNotNull();
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Treasure can immediately be tapped and sacrificed for one mana of any color")
    void treasureTokenHasManaAbility(ManaColor color) {
        harness.setHand(player1, List.of(new PiratesPrize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.handleListChoice(player1, color.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only the caster draws the top two cards and receives exactly one Treasure")
    void benefitsOnlyCaster() {
        PiratesPrize first = new PiratesPrize();
        PiratesPrize second = new PiratesPrize();
        PiratesPrize third = new PiratesPrize();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new PiratesPrize()));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize);
    }

    @Test
    @DisplayName("A tapped Treasure cannot be sacrificed for mana through its tap ability")
    void tappedTreasureCannotActivate() {
        harness.setHand(player1, List.of(new PiratesPrize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent treasure = findPermanent(player1, "Treasure");
        treasure.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new PiratesPrize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertInGraveyard(player1, "Pirate's Prize");
        assertThat(gd.stack).isEmpty();
    }

}
