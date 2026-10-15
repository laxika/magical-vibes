package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SageOfMysteries.class, GloriousAnthem.class, Forest.class, NyxbornCourser.class})
class SageOfMysteriesTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control makes a target player mill two cards")
    void ownEnchantmentEntryMillsTargetPlayer() {
        harness.addToBattlefield(player1, new SageOfMysteries());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new SageOfMysteries());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An enchantment creature entering can make its controller mill")
    void enchantmentCreatureCanMillController() {
        harness.addToBattlefield(player1, new SageOfMysteries());
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new NyxbornCourser()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Milling fewer than two remaining cards mills only the available cards")
    void shortLibraryMillsOnlyAvailableCards(int librarySize) {
        harness.addToBattlefield(player1, new SageOfMysteries());
        List<Forest> library = librarySize == 0 ? List.of() : List.of(new Forest());
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new NyxbornCourser()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An ordinary creature entering does not trigger constellation")
    void nonEnchantmentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new SageOfMysteries());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new SageOfMysteries()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
