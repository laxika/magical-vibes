package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomousHierophant.class, Forest.class, VoraciousTyphon.class})
class VenomousHierophantTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it mills three cards")
    void entersAndMillsThreeCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new VenomousHierophant());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> (Object) card.getClass())
                .containsExactly(Forest.class, Forest.class, Forest.class);
    }

    @Test
    @DisplayName("The trigger mills only the controller's top three cards")
    void millsOnlyControllersTopThreeCards() {
        Forest top = new Forest();
        VoraciousTyphon second = new VoraciousTyphon();
        Forest third = new Forest();
        VoraciousTyphon remaining = new VoraciousTyphon();
        Forest opponentsCard = new Forest();
        harness.setLibrary(player2, List.of(top, second, third, remaining));
        harness.setLibrary(player1, List.of(opponentsCard));

        harness.enterBattlefieldAndReturn(player2, new VenomousHierophant());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(top, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A library with fewer than three cards mills all remaining cards")
    void millsAllCardsFromShortLibrary() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        harness.enterBattlefieldAndReturn(player1, new VenomousHierophant());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        harness.assertOnBattlefield(player1, "Venomous Hierophant");
    }

    @Test
    @DisplayName("Entering with an empty library still resolves normally")
    void emptyLibraryDoesNotPreventResolution() {
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new VenomousHierophant());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Venomous Hierophant");
    }

    @Test
    @DisplayName("Deathtouch destroys a blocker whose toughness exceeds the damage dealt")
    void deathtouchDestroysLargerBlocker() {
        Permanent attacker = addCreatureReady(player1, new VenomousHierophant());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Venomous Hierophant");
        harness.assertInGraveyard(player2, "Voracious Typhon");
        harness.assertNotOnBattlefield(player1, "Venomous Hierophant");
        harness.assertNotOnBattlefield(player2, "Voracious Typhon");
        harness.assertLife(player2, 20);
    }
}
