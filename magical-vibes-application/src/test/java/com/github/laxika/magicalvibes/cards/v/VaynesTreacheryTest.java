package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientAdamantoise;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IronGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VaynesTreachery.class, GrizzlyBears.class, HillGiant.class, Spellbook.class,
        AncientAdamantoise.class, IronGiant.class})
class VaynesTreacheryTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, target creature gets -2/-2 until end of turn")
    void givesMinusTwoMinusTwoWithoutKicker() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new VaynesTreachery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("With kicker, sacrificing an artifact gives target creature -6/-6")
    void kickedWithArtifactGivesMinusSixMinusSix() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new Spellbook()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new VaynesTreachery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castKickedInstantWithSacrifice(player1, 0, targetId, sacrificeId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("With kicker, sacrificing a creature gives target creature -6/-6")
    void kickedWithCreatureGivesMinusSixMinusSix() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new VaynesTreachery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castKickedInstantWithSacrifice(player1, 0, targetId, sacrificeId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The temporary debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new HillGiant()).getId();
        harness.setHand(player1, List.of(new VaynesTreachery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, targetId);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Kicker replaces the debuff with exactly -6/-6, which expires at end of turn")
    void kickedDebuffReplacesBaseDebuffAndExpires() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AncientAdamantoise());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new IronGiant());
        harness.setHand(player1, List.of(new VaynesTreachery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Iron Giant");
        harness.assertNotOnBattlefield(player1, "Iron Giant");
        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(14);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(target.getEffectivePower()).isEqualTo(8);
        assertThat(target.getEffectiveToughness()).isEqualTo(20);
    }

    @Test
    @DisplayName("The targeted creature can be sacrificed for kicker, leaving the spell with no legal target")
    void canSacrificeTargetForKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IronGiant());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new IronGiant());
        harness.setHand(player1, List.of(new VaynesTreachery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), target.getId());
        harness.assertInGraveyard(player1, "Iron Giant");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vayne's Treachery");
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Iron Giant");
        assertThat(other.getEffectivePower()).isEqualTo(6);
        assertThat(other.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("An opponent's permanent cannot pay the kicker sacrifice cost")
    void cannotSacrificeOpponentsPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronGiant());
        harness.setHand(player1, List.of(new VaynesTreachery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifice(
                player1, 0, target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Iron Giant");
        harness.assertInHand(player1, "Vayne's Treachery");
        assertThat(gd.stack).isEmpty();
    }
}
