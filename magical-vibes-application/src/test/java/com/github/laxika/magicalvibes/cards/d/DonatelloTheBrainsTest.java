package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MyrBattlesphere;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonatelloTheBrains.class, MyrBattlesphere.class, WilyGoblin.class})
class DonatelloTheBrainsTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one Mutagen token to a token creation event")
    void addsMutagenToTokenCreation() {
        harness.addToBattlefield(player1, new DonatelloTheBrains());
        castMyrBattlesphere(player1);

        assertThat(findPermanents(player1, "Myr")).hasSize(4);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Adds a Mutagen to a noncreature token creation event")
    void addsMutagenToNoncreatureTokenCreation() {
        harness.addToBattlefield(player1, new DonatelloTheBrains());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Does not affect an opponent's token creation")
    void doesNotAffectOpponentTokenCreation() {
        harness.addToBattlefield(player1, new DonatelloTheBrains());
        castMyrBattlesphere(player2);

        assertThat(findPermanents(player2, "Myr")).hasSize(4);
        assertThat(findPermanents(player2, "Mutagen")).isEmpty();
    }

    private void castMyrBattlesphere(Player player) {
        harness.setHand(player, List.of(new MyrBattlesphere()));
        harness.addMana(player, ManaColor.COLORLESS, 7);
        harness.forceActivePlayer(player);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
