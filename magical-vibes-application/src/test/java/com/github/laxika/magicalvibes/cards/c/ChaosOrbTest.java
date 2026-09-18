package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaosOrb.class, AngelicChorus.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class})
class ChaosOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all nontoken permanents and itself while sparing tokens")
    void destroysNontokenPermanentsAndItself() {
        harness.addToBattlefield(player1, new ChaosOrb());
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card token = new GrizzlyBears();
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chaos Orb");
        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The source's destruction respects regeneration")
    void sourceDestructionRespectsRegeneration() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new ChaosOrb());
        harness.addToBattlefield(player2, new GrizzlyBears());
        orb.setRegenerationShield(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chaos Orb");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
