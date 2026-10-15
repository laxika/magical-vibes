package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenegadeDoppelganger.class, GrizzlyBears.class, HillGiant.class,
        PincherBeetles.class, SporecapSpider.class})
class RenegadeDoppelgangerTest extends BaseCardTest {

    @Test
    @DisplayName("May become a copy of another creature you control that enters")
    void mayBecomeCopyOfEnteringCreature() {
        Permanent doppelganger = addDoppelganger();
        castCreature(player1, new GrizzlyBears(), ManaColor.GREEN, 2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, doppelganger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, doppelganger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the copy choice leaves the creature unchanged")
    void mayBeDeclined() {
        Permanent doppelganger = addDoppelganger();
        castCreature(player1, new GrizzlyBears(), ManaColor.GREEN, 2);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, doppelganger)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, doppelganger)).isEqualTo(1);
    }

    @Test
    @DisplayName("The copy does not retain the trigger ability during the turn")
    void copyDoesNotRetainTriggerAbility() {
        Permanent doppelganger = addDoppelganger();
        harness.setHand(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, doppelganger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, doppelganger)).isEqualTo(2);
    }

    @Test
    @DisplayName("The temporary copy reverts at end of turn")
    void copyRevertsAtEndOfTurn() {
        Permanent doppelganger = addDoppelganger();
        castCreature(player1, new GrizzlyBears(), ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, doppelganger)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, doppelganger)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature entering under an opponent's control does not trigger it")
    void opponentCreatureDoesNotTrigger() {
        Permanent doppelganger = addDoppelganger();
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectivePower(gd, doppelganger)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, doppelganger)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can copy a creature with shroud because the ability does not target")
    void copiesCreatureWithShroud() {
        Permanent doppelganger = addDoppelganger();
        castCreature(player1, new PincherBeetles(), ManaColor.GREEN, 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, doppelganger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, doppelganger)).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses last known characteristics when the entering creature leaves before resolution")
    void copiesCreatureThatLeftBattlefield() {
        Permanent doppelganger = addDoppelganger();
        harness.setHand(player1, List.of(new SporecapSpider()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent spider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SporecapSpider)
                .findFirst().orElseThrow();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spider);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, doppelganger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, doppelganger)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining leaves the ability available for the next entering creature")
    void decliningAllowsAnotherTrigger() {
        Permanent doppelganger = addDoppelganger();
        castCreature(player1, new SporecapSpider(), ManaColor.GREEN, 3);
        harness.handleMayAbilityChosen(player1, false);

        castCreature(player1, new SporecapSpider(), ManaColor.GREEN, 3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, doppelganger)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, doppelganger)).isEqualTo(5);
    }

    @Test
    @DisplayName("Entering by itself does not trigger its own copy ability")
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new RenegadeDoppelganger()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addDoppelganger() {
        return harness.addToBattlefieldAndReturn(player1, new RenegadeDoppelganger());
    }

    private void castCreature(Player player, Card creature, ManaColor manaColor, int manaAmount) {
        harness.setHand(player, List.of(creature));
        harness.addMana(player, manaColor, manaAmount);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
