package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AllIsDust;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SteelSeraph;
import com.github.laxika.magicalvibes.cards.t.TawnossTinkering;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiberatorUrzasBattlethopter.class, GrizzlyBears.class, Ornithopter.class,
        AllIsDust.class, SteelSeraph.class, TawnossTinkering.class})
class LiberatorUrzasBattlethopterTest extends BaseCardTest {

    private Permanent addLiberator(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LiberatorUrzasBattlethopter());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void castsArtifactSpellWithFlash() {
        addLiberator(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    void doesNotGiveFlashToColoredNonartifactSpells() {
        addLiberator(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void putsCounterWhenManaSpentExceedsCurrentPower() {
        Permanent liberator = addLiberator(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(liberator.getPlusOnePlusOneCounters()).isEqualTo(1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(liberator.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void comparesManaSpentWithPowerOnly() {
        Permanent liberator = addLiberator(player1);
        liberator.setPowerModifier(3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(liberator.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void liberatorItselfHasFlashAndDoesNotTriggerForItsOwnCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new LiberatorUrzasBattlethopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Liberator, Urza's Battlethopter")
                .getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void castsColorlessNonartifactSorceryDuringOpponentsTurn() {
        Permanent liberator = addLiberator(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new AllIsDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "All Is Dust");
        assertThat(liberator.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void castsColoredPrototypeArtifactDuringOpponentsTurn() {
        Permanent liberator = addLiberator(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SteelSeraph()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Steel Seraph");
        assertThat(liberator.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void comparesActualPrototypeManaSpentRatherThanNormalManaCost() {
        Permanent liberator = addLiberator(player1);
        liberator.setPowerModifier(2);
        harness.setHand(player1, List.of(new SteelSeraph()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Steel Seraph");
        assertThat(liberator.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void doesNotTriggerForZeroManaSpent() {
        Permanent liberator = addLiberator(player1);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(liberator.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void doesNotGrantFlashToOpponentsOrTriggerForTheirSpells() {
        Permanent liberator = addLiberator(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player2, List.of(new Ornithopter()));

        assertThatThrownBy(() -> harness.castArtifact(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(liberator.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void rechecksPowerWhenTriggerResolves() {
        Permanent liberator = addLiberator(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new TawnossTinkering()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, liberator.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(liberator.getPlusOnePlusOneCounters()).isEqualTo(3);
    }
}
