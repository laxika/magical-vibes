package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class HarshJudgmentTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects damage from an instant of the chosen color to its controller")
    void redirectsChosenColorInstantDamage() {
        castHarshJudgment(player1, CardColor.RED);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Redirects damage from a sorcery of the chosen color to its controller")
    void redirectsChosenColorSorceryDamage() {
        castHarshJudgment(player1, CardColor.RED);
        harness.setHand(player2, List.of(new LavaSpike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Does not redirect a spell of another color")
    void doesNotRedirectOtherColorSpellDamage() {
        castHarshJudgment(player1, CardColor.RED);
        harness.setHand(player2, List.of(new HornetSting()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not redirect damage from an activated ability")
    void doesNotRedirectActivatedAbilityDamage() {
        castHarshJudgment(player1, CardColor.RED);
        Permanent pinger = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        pinger.setSummoningSick(false);
        harness.forceActivePlayer(player2);

        int pingerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(pinger);
        harness.activateAbility(player2, pingerIndex, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not redirect a matching spell controlled by the protected player")
    void doesNotLoopWhenProtectedPlayerControlsSpell() {
        castHarshJudgment(player1, CardColor.RED);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    private void castHarshJudgment(Player player, CardColor color) {
        harness.setHand(player, List.of(new HarshJudgment()));
        harness.addMana(player, ManaColor.WHITE, 4);
        harness.castEnchantment(player, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player, color.name());
    }
}
