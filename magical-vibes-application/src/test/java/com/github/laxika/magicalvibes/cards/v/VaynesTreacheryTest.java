package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({VaynesTreachery.class, GrizzlyBears.class, HillGiant.class, Spellbook.class})
class VaynesTreacheryTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, target creature gets -2/-2 until end of turn")
    void givesMinusTwoMinusTwoWithoutKicker() {
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new VaynesTreachery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("With kicker, sacrificing an artifact gives target creature -6/-6")
    void kickedWithArtifactGivesMinusSixMinusSix() {
        harness.addToBattlefield(player1, new Spellbook());
        UUID sacrificeId = harness.getPermanentId(player1, "Spellbook");
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID sacrificeId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
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
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.setHand(player1, List.of(new VaynesTreachery()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }
}
