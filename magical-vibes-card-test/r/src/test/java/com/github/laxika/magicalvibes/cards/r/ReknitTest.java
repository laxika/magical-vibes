package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.e.ElsewhereFlask;
import com.github.laxika.magicalvibes.cards.s.SmashToSmithereens;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Reknit.class, GrizzlyBears.class, FountainOfYouth.class, ElsewhereFlask.class, SmashToSmithereens.class})
class ReknitTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reknit grants a regeneration shield to target creature")
    void grantsShieldToCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Reknit()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Reknit can target a noncreature permanent")
    void canTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Reknit()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castAndResolveInstant(player1, 0, fountainId);

        Permanent fountain = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(fountain.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Reknit can be paid with white mana for the hybrid symbol")
    void payableWithWhiteMana() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Reknit()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }
    @Test
    @DisplayName("Reknit protects an opponent's artifact only from the next destruction")
    void protectsOpponentsArtifactFromOneDestruction() {
        Permanent flask = harness.addToBattlefieldAndReturn(player2, new ElsewhereFlask());
        harness.setHand(player1, List.of(new Reknit(), new SmashToSmithereens(), new SmashToSmithereens()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, flask.getId());

        assertThat(flask.isTapped()).isFalse();
        assertThat(flask.getRegenerationShield()).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, flask.getId());

        harness.assertOnBattlefield(player2, "Elsewhere Flask");
        harness.assertNotInGraveyard(player2, "Elsewhere Flask");
        assertThat(flask.isTapped()).isTrue();
        assertThat(flask.getRegenerationShield()).isZero();
        harness.assertLife(player2, 17);

        harness.castAndResolveInstant(player1, 0, flask.getId());

        harness.assertNotOnBattlefield(player2, "Elsewhere Flask");
        harness.assertInGraveyard(player2, "Elsewhere Flask");
        harness.assertLife(player2, 14);
    }
}
