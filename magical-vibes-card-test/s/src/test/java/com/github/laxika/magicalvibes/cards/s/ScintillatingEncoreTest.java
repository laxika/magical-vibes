package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScintillatingEncore.class, DoomBlade.class, GrizzlyBears.class, TurnToFrog.class})
class ScintillatingEncoreTest extends BaseCardTest {

    @Test
    @DisplayName("Perpetually gives a creature you control +2/+0")
    void perpetuallyBoostsTargetCreature() {
        Permanent target = addCreature(player1);

        castEncore(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Returns the creature tapped under its owner's control when it dies this turn")
    void returnsTargetTappedWhenItDiesThisTurn() {
        Permanent target = addCreature(player1);
        Card targetCard = target.getCard();

        harness.setHand(player1, List.of(new ScintillatingEncore(), new DoomBlade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(targetCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreature(player2);
        harness.setHand(player1, List.of(new ScintillatingEncore()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Perpetual +2/+0 applies after an effect sets base power and toughness")
    void perpetualBoostSurvivesBasePowerToughnessOverride() {
        Permanent target = addCreature(player1);
        castEncore(target.getId());

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Encore caster controls the delayed trigger even when the creature has another owner")
    void casterControlsReturnTriggerForBorrowedCreature() {
        Permanent target = addCreature(player1);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        castEncore(target.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement().satisfies(returned -> {
                    assertThat(returned.isTapped()).isTrue();
                    assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
                });
    }

    @Test
    @DisplayName("The delayed return has Encore as its source")
    void encoreIsSourceOfReturnTrigger() {
        Permanent target = addCreature(player1);
        castEncore(target.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(ScintillatingEncore.class);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The boost persists but the return expires after the turn")
    void returnExpiresAtEndOfTurn() {
        Permanent target = addCreature(player1);
        castEncore(target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The returned creature does not return again when it dies a second time")
    void returnedCreatureDoesNotReturnAgain() {
        Permanent target = addCreature(player1);
        castEncore(target.getId());

        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        UUID returnedId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, returnedId);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castEncore(UUID targetId) {
        harness.setHand(player1, List.of(new ScintillatingEncore()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
