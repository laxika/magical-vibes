package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShrewdNegotiation.class, Millstone.class, IcyManipulator.class,
        HillGiant.class, GrizzlyBears.class, PlatinumAngel.class, Unsummon.class})
class ShrewdNegotiationTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new ShrewdNegotiation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Exchanges control of an artifact for an opponent's artifact")
    void exchangesArtifacts() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponent.getId()));

        harness.assertOnBattlefield(player2, "Millstone");
        harness.assertOnBattlefield(player1, "Icy Manipulator");
    }

    @Test
    @DisplayName("Exchanges control of an artifact for an opponent's creature")
    void exchangesArtifactForCreature() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponent.getId()));

        harness.assertOnBattlefield(player2, "Millstone");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a creature you control as the first target")
    void firstTargetMustBeAnArtifactYouControl() {
        prepare();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(ownCreature.getId(), opponentArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact you control");
    }

    @Test
    @DisplayName("Cannot target a permanent you control as the second target")
    void secondTargetMustNotBeControlledByCaster() {
        prepare();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent alsoOwn = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(ownArtifact.getId(), alsoOwn.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }

    @Test
    @DisplayName("Cannot offer an opponent's artifact as the first target")
    void firstTargetMustBeControlledByCaster() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact you control");
    }

    @Test
    @DisplayName("No exchange occurs when the opponent's target leaves the battlefield")
    void noExchangeWhenSecondTargetLeaves() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.castAndResolveInstant(player2, 0, opponent.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Millstone");
        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInHand(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Shrewd Negotiation");
    }

    @Test
    @DisplayName("No exchange occurs when your artifact creature leaves the battlefield")
    void noExchangeWhenFirstTargetLeaves() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new PlatinumAngel());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(own.getId(), opponent.getId()));
        harness.castAndResolveInstant(player2, 0, own.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Platinum Angel");
        harness.assertNotOnBattlefield(player2, "Platinum Angel");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Shrewd Negotiation");
    }

    @Test
    @DisplayName("Exchanges a cheap artifact for a more expensive artifact creature")
    void exchangesArtifactForMoreExpensiveArtifactCreature() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new PlatinumAngel());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponent.getId()));

        harness.assertOnBattlefield(player2, "Millstone");
        harness.assertNotOnBattlefield(player1, "Millstone");
        harness.assertOnBattlefield(player1, "Platinum Angel");
        harness.assertNotOnBattlefield(player2, "Platinum Angel");
    }
}
