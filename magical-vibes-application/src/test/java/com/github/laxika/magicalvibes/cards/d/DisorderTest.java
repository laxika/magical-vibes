package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AbsoluteGrace;
import com.github.laxika.magicalvibes.cards.a.AngelicPage;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.h.HeraldOfSerra;
import com.github.laxika.magicalvibes.cards.v.VoiceOfLaw;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Disorder.class, AbsoluteGrace.class, AngelicPage.class, GorillaWarrior.class,
        HeraldOfSerra.class, VoiceOfLaw.class})
class DisorderTest extends BaseCardTest {

    private void castDisorder() {
        harness.castFromHand(player1, new Disorder(), "{1}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 2 damage to white creatures, leaving non-white creatures unharmed")
    void damagesWhiteCreaturesOnly() {
        harness.addToBattlefield(player2, new AngelicPage());   // 1/1 white
        harness.addToBattlefield(player2, new GorillaWarrior()); // 3/2 green

        castDisorder();

        // The 1/1 white creature dies; the green creature is untouched.
        harness.assertNotOnBattlefield(player2, "Angelic Page");
        harness.assertOnBattlefield(player2, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Deals 2 damage to each player controlling a white creature; others untouched")
    void damagesControllersOfWhiteCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GorillaWarrior()); // green — controller safe
        harness.addToBattlefield(player2, new HeraldOfSerra());  // 3/4 white — controller takes 2

        castDisorder();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        // 3/4 white survives 2 damage.
        harness.assertOnBattlefield(player2, "Herald of Serra");
    }

    @Test
    @DisplayName("Controller still takes damage even if their only white creature dies (simultaneous)")
    void controllerDamagedEvenWhenWhiteCreatureDies() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AngelicPage()); // 1/1 white, dies to the 2 damage

        castDisorder();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Angelic Page");
    }

    @Test
    @DisplayName("Damages both players and all of their white creatures")
    void damagesBothPlayersAndTheirWhiteCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AngelicPage());
        harness.addToBattlefield(player2, new AngelicPage());

        castDisorder();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Angelic Page");
        harness.assertNotOnBattlefield(player2, "Angelic Page");
    }

    @Test
    @DisplayName("Does not affect a white noncreature")
    void ignoresWhiteNoncreatures() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AbsoluteGrace());

        castDisorder();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Absolute Grace");
    }

    @Test
    @DisplayName("Protection from red prevents damage to a white creature but not its controller")
    void protectionFromRedPreventsCreatureDamage() {
        harness.setLife(player2, 20);
        var voiceOfLaw = harness.addToBattlefieldAndReturn(player2, new VoiceOfLaw());

        castDisorder();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Voice of Law");
        assertThat(voiceOfLaw.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castDisorder();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Disorder");
    }
}
