package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LashOfMalice;
import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
import com.github.laxika.magicalvibes.cards.s.ScurridColony;
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

@CardUsed({ChargeThrough.class, ScurridColony.class, LetterOfAcceptance.class, LashOfMalice.class})
class ChargeThroughTest extends BaseCardTest {

    @Test
    @DisplayName("Grants trample to the target creature and draws a card")
    void grantsTrampleAndDraws() {
        harness.addToBattlefield(player1, new ScurridColony());
        harness.setHand(player1, List.of(new ChargeThrough()));
        harness.setLibrary(player1, List.of(new ScurridColony()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Scurrid Colony");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Trample wears off at cleanup")
    void trampleWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new ScurridColony());
        harness.setHand(player1, List.of(new ChargeThrough()));
        harness.setLibrary(player1, List.of(new ScurridColony()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Scurrid Colony");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ScurridColony());
        harness.addToBattlefield(player1, new LetterOfAcceptance());
        harness.setHand(player1, List.of(new ChargeThrough()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Letter of Acceptance");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target an opponent's creature while only the caster draws")
    void targetsOpponentsCreatureAndCasterDraws() {
        harness.addToBattlefield(player2, new ScurridColony());
        harness.setHand(player1, List.of(new ChargeThrough()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new LetterOfAcceptance()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Scurrid Colony");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        harness.assertInHand(player1, "Letter of Acceptance");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw when its only target dies before resolution")
    void doesNotDrawWhenTargetDies() {
        harness.addToBattlefield(player1, new ScurridColony());
        harness.setHand(player1, List.of(new ChargeThrough()));
        harness.setHand(player2, List.of(new LashOfMalice()));
        harness.setLibrary(player1, List.of(new LetterOfAcceptance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Scurrid Colony");
        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertNotOnBattlefield(player1, "Scurrid Colony");
        harness.assertInGraveyard(player1, "Scurrid Colony");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Charge Through");
        assertThat(gd.stack).isEmpty();
    }
}
