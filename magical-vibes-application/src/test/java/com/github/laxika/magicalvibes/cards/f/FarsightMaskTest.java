package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.ShrapnelBlast;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlphaMyr.class, FarsightMask.class, Forest.class, Ornithopter.class, ShrapnelBlast.class})
class FarsightMaskTest extends BaseCardTest {

    @Test
    void opponentDamageMayDraw() {
        harness.addToBattlefield(player1, new FarsightMask());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player2, List.of(new ShrapnelBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstantWithSacrifice(player2, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentCombatDamageMayDraw() {
        harness.addToBattlefield(player1, new FarsightMask());
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player2, new AlphaMyr());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new FarsightMask());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player2, List.of(new ShrapnelBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstantWithSacrifice(player2, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void tappedMaskDoesNotTrigger() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new FarsightMask());
        mask.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player2, List.of(new ShrapnelBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstantWithSacrifice(player2, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void ownDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new FarsightMask());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new ShrapnelBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithSacrifice(player1, 0, player1.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
