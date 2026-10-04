package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({HaazdaOfficer.class, GreenwoodSentinel.class})
class HaazdaOfficerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature you control +1/+1 until end of turn")
    void etbBoostsTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new HaazdaOfficer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player1, "Greenwood Sentinel");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent sentinel = findPermanent(player1, "Greenwood Sentinel");
        assertThat(sentinel.getEffectivePower()).isEqualTo(3);
        assertThat(sentinel.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new HaazdaOfficer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player1, "Greenwood Sentinel");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent sentinel = findPermanent(player1, "Greenwood Sentinel");
        assertThat(sentinel.getEffectivePower()).isEqualTo(2);
        assertThat(sentinel.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new HaazdaOfficer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Greenwood Sentinel");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class);
        Permanent officer = findPermanent(player1, "Haazda Officer");
        int powerBefore = officer.getEffectivePower();
        harness.handlePermanentChosen(player1, officer.getId());
        harness.passBothPriorities();
        assertThat(officer.getEffectivePower()).isEqualTo(powerBefore + 1);
        assertThat(findPermanent(player2, "Greenwood Sentinel").getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast on an empty battlefield and target itself when it enters")
    void entersWithoutTarget() {
        harness.setHand(player1, List.of(new HaazdaOfficer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        Permanent officer = findPermanent(player1, "Haazda Officer");
        int powerBefore = officer.getEffectivePower();
        int toughnessBefore = officer.getEffectiveToughness();
        harness.handlePermanentChosen(player1, officer.getId());
        harness.passBothPriorities();
        assertThat(officer.getEffectivePower()).isEqualTo(powerBefore + 1);
        assertThat(officer.getEffectiveToughness()).isEqualTo(toughnessBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if the target leaves before resolution")
    void etbFizzlesIfTargetLeaves() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new HaazdaOfficer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player1, "Greenwood Sentinel");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(targetId));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Target can be chosen after casting even with another creature already present")
    void canChooseSelfAfterCastingWithAnotherCreaturePresent() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new HaazdaOfficer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        Permanent officer = findPermanent(player1, "Haazda Officer");
        int powerBefore = officer.getEffectivePower();
        int toughnessBefore = officer.getEffectiveToughness();
        harness.handlePermanentChosen(player1, officer.getId());
        harness.passBothPriorities();

        assertThat(officer.getEffectivePower()).isEqualTo(powerBefore + 1);
        assertThat(officer.getEffectiveToughness()).isEqualTo(toughnessBefore + 1);
        assertThat(findPermanent(player1, "Greenwood Sentinel").getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("An early target hint must not lock the entry trigger to a creature that has left")
    void choosesLegalTargetOnEntryAfterEarlyTargetLeaves() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new HaazdaOfficer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, sentinel.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sentinel);
        harness.passBothPriorities();

        Permanent officer = findPermanent(player1, "Haazda Officer");
        int powerBefore = officer.getEffectivePower();
        int toughnessBefore = officer.getEffectiveToughness();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, officer.getId());
        harness.passBothPriorities();

        assertThat(officer.getEffectivePower()).isEqualTo(powerBefore + 1);
        assertThat(officer.getEffectiveToughness()).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Trigger still resolves if Haazda Officer leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new HaazdaOfficer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent sentinel = findPermanent(player1, "Greenwood Sentinel");
        harness.handlePermanentChosen(player1, sentinel.getId());
        UUID officerId = harness.getPermanentId(player1, "Haazda Officer");
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(officerId));
        harness.passBothPriorities();

        assertThat(sentinel.getEffectivePower()).isEqualTo(3);
        assertThat(sentinel.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Target becomes illegal if an opponent gains control before resolution")
    void triggerDoesNotBoostCreatureNoLongerControlled() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new HaazdaOfficer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, sentinel.getId());
        gd.playerBattlefields.get(player1.getId()).remove(sentinel);
        gd.playerBattlefields.get(player2.getId()).add(sentinel);
        harness.passBothPriorities();

        assertThat(sentinel.getEffectivePower()).isEqualTo(2);
        assertThat(sentinel.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
