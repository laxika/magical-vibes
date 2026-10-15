package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plummet;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SungracePegasus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EphemeralShields.class, RuneclawBear.class, Forest.class, SungracePegasus.class, Plummet.class})
class EphemeralShieldsTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gains indestructible until end of turn")
    void grantsIndestructibleUntilEndOfTurn() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new EphemeralShields()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

    }

    @Test
    @DisplayName("Convoke taps a creature to help cast the spell")
    void castsWithConvoke() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new EphemeralShields()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        UUID targetId = battlefield.get(0).getId();
        UUID convokeId = battlefield.get(1).getId();

        harness.castInstantWithConvoke(player1, 0, List.of(targetId), List.of(convokeId));

        assertThat(battlefield.get(1).isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(battlefield.get(0).hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new EphemeralShields()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The indestructible grant expires when the turn ends")
    void indestructibleExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new EphemeralShields()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castAndResolveInstant(player1, 0, bear.getId());
        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Convoke can pay the entire cost using summoning-sick creatures, including the target")
    void paysEntireCostWithConvoke() {
        harness.addToBattlefield(player1, new SungracePegasus());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new EphemeralShields()));
        Permanent pegasus = gd.playerBattlefields.get(player1.getId()).get(0);
        Permanent bear = gd.playerBattlefields.get(player1.getId()).get(1);
        pegasus.setSummoningSick(true);
        bear.setSummoningSick(true);

        harness.castInstantWithConvoke(player1, 0, List.of(pegasus.getId()),
                List.of(pegasus.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(pegasus.isTapped()).isTrue();
        assertThat(bear.isTapped()).isTrue();
        assertThat(pegasus.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Ephemeral Shields");
    }

    @Test
    @DisplayName("Can grant indestructible to an opponent's creature")
    void targetsOpponentsCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new EphemeralShields()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent bear = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Indestructible protects the target from a destroy spell")
    void protectsFromDestruction() {
        harness.addToBattlefield(player1, new SungracePegasus());
        UUID targetId = harness.getPermanentId(player1, "Sungrace Pegasus");
        harness.setHand(player1, List.of(new EphemeralShields(), new Plummet()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player1, "Sungrace Pegasus");
        harness.assertNotInGraveyard(player1, "Sungrace Pegasus");
        harness.assertInGraveyard(player1, "Plummet");
    }
}
