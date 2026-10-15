package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.d.DouserOfLights;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PitilessGorgon.class, AvatarOfMight.class, DouserOfLights.class})
class PitilessGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Pitiless Gorgon destroys a larger creature in combat")
    void deathtouchDestroysLargerCreature() {
        harness.addToBattlefield(player1, new PitilessGorgon());
        harness.addToBattlefield(player2, new AvatarOfMight());

        var attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        var blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Pitiless Gorgon destroys a larger attacker when blocking")
    void deathtouchDestroysLargerAttacker() {
        var attacker = addCreatureReady(player1, new DouserOfLights());
        attacker.setAttacking(true);
        var blocker = harness.addToBattlefieldAndReturn(player2, new PitilessGorgon());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Douser of Lights");
        harness.assertInGraveyard(player2, "Pitiless Gorgon");
        harness.assertLife(player2, 20);
    }
}
