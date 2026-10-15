package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvacynAngelOfHope;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HumanFrailty.class, EliteVanguard.class, GrizzlyBears.class, Forest.class,
        MoorlandInquisitor.class, Cloudshift.class, AvacynAngelOfHope.class})
class HumanFrailtyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a targeted Human creature")
    void destroysHumanCreature() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new EliteVanguard()).getId();

        harness.setHand(player1, List.of(new HumanFrailty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target);

        harness.assertInGraveyard(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Cannot target a non-Human creature")
    void cannotTargetNonHumanCreature() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new HumanFrailty()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(target)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        UUID land = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.setHand(player1, List.of(new HumanFrailty()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy a Human controlled by its caster")
    void destroysOwnHuman() {
        UUID target = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor()).getId();
        harness.setHand(player1, List.of(new HumanFrailty()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target);

        harness.assertNotOnBattlefield(player1, "Moorland Inquisitor");
        harness.assertInGraveyard(player1, "Moorland Inquisitor");
        harness.assertInGraveyard(player1, "Human Frailty");
    }

    @Test
    @DisplayName("Does not destroy an indestructible Human")
    void respectsIndestructible() {
        harness.addToBattlefield(player2, new AvacynAngelOfHope());
        UUID target = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor()).getId();
        harness.setHand(player1, List.of(new HumanFrailty()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target);

        harness.assertOnBattlefield(player2, "Moorland Inquisitor");
        harness.assertNotInGraveyard(player2, "Moorland Inquisitor");
        harness.assertInGraveyard(player1, "Human Frailty");
    }

    @Test
    @DisplayName("A blinked Human is a new permanent and escapes destruction")
    void blinkedHumanIsNoLongerTheTarget() {
        UUID target = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor()).getId();
        harness.setHand(player1, List.of(new HumanFrailty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, target);

        harness.setHand(player2, List.of(new Cloudshift()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Moorland Inquisitor");
        harness.assertNotInGraveyard(player2, "Moorland Inquisitor");
        harness.assertInGraveyard(player1, "Human Frailty");
        harness.assertInGraveyard(player2, "Cloudshift");
    }
}
