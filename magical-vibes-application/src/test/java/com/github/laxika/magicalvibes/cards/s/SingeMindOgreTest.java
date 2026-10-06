package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lavalanche;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SingeMindOgre.class, GrizzlyBears.class, Lavalanche.class, Terminate.class})
class SingeMindOgreTest extends BaseCardTest {

    private void castSingeMindOgre(java.util.UUID targetPlayerId) {
        harness.setHand(player1, new ArrayList<>(List.of(new SingeMindOgre())));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);
    }

    @Test
    @DisplayName("ETB makes target player lose life equal to the revealed card's mana value")
    void etbLosesLifeEqualToManaValue() {
        Card revealed = new GrizzlyBears(); // sole hand card -> revealed deterministically
        harness.setHand(player2, new ArrayList<>(List.of(revealed)));
        int manaValue = revealed.getManaValue();
        int lifeBefore = gd.getLife(player2.getId());

        castSingeMindOgre(player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.getLife(player2.getId())).isLessThan(lifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - manaValue);
        // Revealing does not remove the card from the target's hand.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB does nothing when the target player's hand is empty")
    void etbEmptyHandNoLifeLoss() {
        harness.setHand(player2, new ArrayList<>());
        int lifeBefore = gd.getLife(player2.getId());

        castSingeMindOgre(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The controller can target themselves")
    void canTargetController() {
        castSingeMindOgre(player1.getId());
        Card revealed = new SingeMindOgre();
        harness.setHand(player1, List.of(revealed));
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 4);
        harness.assertLife(player2, opponentLifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("X in the revealed card's mana cost counts as zero in hand")
    void xInHandCountsAsZero() {
        Card revealed = new Lavalanche();
        harness.setHand(player2, List.of(revealed));
        int lifeBefore = gd.getLife(player2.getId());

        castSingeMindOgre(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 3);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("The trigger reveals from the hand as it exists on resolution")
    void usesHandAtResolution() {
        harness.setHand(player2, List.of(new Terminate()));
        int lifeBefore = gd.getLife(player2.getId());
        castSingeMindOgre(player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, lifeBefore);

        Card replacement = new SingeMindOgre();
        harness.setHand(player2, List.of(replacement));
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 4);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(replacement);
    }

    @Test
    @DisplayName("The trigger resolves even if the Ogre is destroyed in response")
    void triggerSurvivesSourceRemoval() {
        Card revealed = new Lavalanche();
        harness.setHand(player2, List.of(revealed));
        int lifeBefore = gd.getLife(player2.getId());
        castSingeMindOgre(player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Singe-Mind Ogre"));
        harness.assertNotOnBattlefield(player1, "Singe-Mind Ogre");
        harness.assertLife(player2, lifeBefore);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 3);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("Exactly one random card is revealed and determines the life loss")
    void revealsOneOfMultipleCards() {
        Card first = new Terminate();
        Card second = new SingeMindOgre();
        harness.setHand(player2, List.of(first, second));
        int lifeBefore = gd.getLife(player2.getId());

        castSingeMindOgre(player2.getId());
        harness.passBothPriorities();
        int logStart = gd.gameLog.size();
        harness.passBothPriorities();

        List<String> reveals = gd.gameLog.subList(logStart, gd.gameLog.size()).stream()
                .map(entry -> entry.plainText())
                .filter(message -> message.contains(" reveals ") && message.endsWith(" at random."))
                .toList();
        assertThat(reveals).hasSize(1);
        int expectedLoss = reveals.getFirst().contains("Terminate") ? 2 : 4;
        assertThat(reveals.getFirst()).containsAnyOf("Terminate", "Singe-Mind Ogre");
        harness.assertLife(player2, lifeBefore - expectedLoss);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, second);
    }
}
