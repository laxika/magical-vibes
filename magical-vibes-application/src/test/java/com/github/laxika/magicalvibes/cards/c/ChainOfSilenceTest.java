package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChainOfSilence.class, GrizzlyBears.class, Island.class})
class ChainOfSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage dealt by the target creature this turn")
    void preventsAllDamageByTargetCreature() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.permanentsPreventedFromDealingDamage).contains(target.getId());
    }

    @Test
    @DisplayName("Offers the target creature's controller the land sacrifice")
    void offersSacrificeToTargetController() {
        Permanent target = addReadyCreature(player2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(land.getId());
    }

    @Test
    @DisplayName("Sacrificing a land offers the target controller the copy")
    void sacrificingLandOffersCopy() {
        Permanent target = addReadyCreature(player2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, land.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Declining the sacrifice leaves the land and creates no copy")
    void decliningSacrificeCreatesNoCopy() {
        Permanent target = addReadyCreature(player2);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ChainOfSilence()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
