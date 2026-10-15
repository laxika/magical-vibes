package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CreakwoodGhoul;
import com.github.laxika.magicalvibes.cards.c.CrumblingAshes;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.cards.s.Spitemare;
import com.github.laxika.magicalvibes.cards.s.StigmaLasher;
import com.github.laxika.magicalvibes.cards.u.Unmake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        KithkinZealot.class,
        CreakwoodGhoul.class,
        CrumblingAshes.class,
        NettleSentinel.class,
        Spitemare.class,
        StigmaLasher.class,
        Unmake.class
})
class KithkinZealotTest extends BaseCardTest {

    private void castKithkinZealot() {
        harness.setHand(player1, List.of(new KithkinZealot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Gains 1 life for each black and/or red permanent the target opponent controls")
    void gainsLifePerBlackAndRedPermanent() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new CreakwoodGhoul());
        harness.addToBattlefield(player2, new StigmaLasher());
        harness.addToBattlefield(player2, new StigmaLasher());

        castKithkinZealot();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Permanents that are neither black nor red are ignored")
    void ignoresOtherColors() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new StigmaLasher());
        harness.addToBattlefield(player2, new NettleSentinel());

        castKithkinZealot();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Only the target opponent's permanents count, not the caster's")
    void ignoresCastersPermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new StigmaLasher());
        harness.addToBattlefield(player2, new StigmaLasher());

        castKithkinZealot();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Gains no life when the opponent controls no black or red permanents")
    void gainsNothingWithoutBlackOrRed() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new NettleSentinel());

        castKithkinZealot();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counts colored noncreature permanents")
    void countsColoredNoncreaturePermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new CrumblingAshes());

        castKithkinZealot();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Counts permanents as the ETB trigger resolves")
    void countsPermanentsAtTriggerResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new KithkinZealot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new StigmaLasher());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Counts a multicolored black or red permanent once")
    void countsMulticoloredPermanentOnce() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new Spitemare());

        castKithkinZealot();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not count a permanent exiled before the trigger resolves")
    void ignoresPermanentRemovedInResponse() {
        harness.setLife(player1, 20);
        var lasher = harness.addToBattlefieldAndReturn(player2, new StigmaLasher());
        harness.setHand(player1, List.of(new KithkinZealot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unmake()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, lasher.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger still gains life after Kithkin Zealot is exiled")
    void triggerResolvesWithoutSource() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new StigmaLasher());
        harness.setHand(player1, List.of(new KithkinZealot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unmake()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, findPermanent(player1, "Kithkin Zealot").getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new KithkinZealot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
