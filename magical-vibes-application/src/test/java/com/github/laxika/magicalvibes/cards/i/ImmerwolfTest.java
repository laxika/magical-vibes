package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HinterlandHermit;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImmerwolfTest extends BaseCardTest {

    // ===== Static effect: buffs Wolves and Werewolves you control =====

    @Test
    @DisplayName("Other Werewolves you control get +1/+1")
    void buffsOtherWerewolvesYouControl() {
        harness.addToBattlefield(player1, new Immerwolf());
        harness.addToBattlefield(player1, new HinterlandHermit());

        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        // Hinterland Hermit is a 2/1 Human Werewolf -> 3/2 with Immerwolf
        assertThat(gqs.getEffectivePower(gd, hermit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hermit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Other Wolves you control get +1/+1")
    void buffsOtherWolvesYouControl() {
        harness.addToBattlefield(player1, new Immerwolf());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent wolf = findPermanent(player1, "Grizzly Bears");
        wolf.getGrantedSubtypes().add(CardSubtype.WOLF);

        // 2/2 base + 1/1 from Immerwolf
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Immerwolf does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new Immerwolf());

        Permanent immerwolf = findPermanent(player1, "Immerwolf");

        assertThat(gqs.getEffectivePower(gd, immerwolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, immerwolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Wolf/non-Werewolf creatures")
    void doesNotBuffOtherCreatures() {
        harness.addToBattlefield(player1, new Immerwolf());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Werewolves")
    void doesNotBuffOpponentWerewolves() {
        harness.addToBattlefield(player1, new Immerwolf());
        harness.addToBattlefield(player2, new HinterlandHermit());

        Permanent hermit = findPermanent(player2, "Hinterland Hermit");

        assertThat(gqs.getEffectivePower(gd, hermit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hermit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Immerwolves buff each other (both are Wolves)")
    void twoImmerwolvesBuffEachOther() {
        harness.addToBattlefield(player1, new Immerwolf());
        harness.addToBattlefield(player1, new Immerwolf());

        List<Permanent> immerwolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Immerwolf"))
                .toList();

        assertThat(immerwolves).hasSize(2);
        for (Permanent immerwolf : immerwolves) {
            assertThat(gqs.getEffectivePower(gd, immerwolf)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, immerwolf)).isEqualTo(3);
        }
    }

    // ===== Static effect: Non-Human Werewolves you control can't transform =====

    @Test
    @DisplayName("Front-face Human Werewolf you control can still transform to its night side")
    void humanWerewolfCanStillTransformToNight() {
        harness.addToBattlefield(player1, new Immerwolf());
        harness.addToBattlefield(player1, new HinterlandHermit());
        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);

        // The front face is a Human Werewolf, so the restriction doesn't apply: it transforms.
        assertThat(hermit.isTransformed()).isTrue();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Scourge");
    }

    @Test
    @DisplayName("Non-Human Werewolf you control can't transform back to its day side")
    void nonHumanWerewolfCannotTransformBack() {
        harness.addToBattlefield(player1, new Immerwolf());
        harness.addToBattlefield(player1, new HinterlandHermit());
        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player1);
        assertThat(hermit.isTransformed()).isTrue();

        // Two spells would normally transform Hinterland Scourge back, but Immerwolf prevents it.
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceFromUntapToResolveUpkeepTrigger(player2);

        assertThat(hermit.isTransformed()).isTrue();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Scourge");
    }

    @Test
    @DisplayName("Does not prevent opponent's non-Human Werewolves from transforming")
    void doesNotPreventOpponentWerewolfTransform() {
        harness.addToBattlefield(player1, new Immerwolf());
        harness.addToBattlefield(player2, new HinterlandHermit());
        Permanent hermit = findPermanent(player2, "Hinterland Hermit");

        gd.spellsCastLastTurn.clear();
        advanceFromUntapToResolveUpkeepTrigger(player2);
        assertThat(hermit.isTransformed()).isTrue();

        // player1's Immerwolf only restricts Werewolves player1 controls.
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 2);
        advanceFromUntapToResolveUpkeepTrigger(player1);

        assertThat(hermit.isTransformed()).isFalse();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Hermit");
    }

    private void advanceFromUntapToResolveUpkeepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
