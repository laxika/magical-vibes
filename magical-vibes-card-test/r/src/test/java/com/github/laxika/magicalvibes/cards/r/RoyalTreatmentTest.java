package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.t.TatteredRatter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoyalTreatment.class, TatteredRatter.class, RatOut.class})
class RoyalTreatmentTest extends BaseCardTest {

    @Test
    @DisplayName("Grants hexproof and attaches a Royal Role")
    void grantsHexproofAndRoyalRole() {
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        castRoyalTreatment(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> target.getId().equals(permanent.getAttachedTo())))
                .hasSize(1);
    }

    @Test
    @DisplayName("Hexproof wears off at end of turn while the Role remains")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        castRoyalTreatment(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> target.getId().equals(permanent.getAttachedTo())))
                .hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent target = addCreatureReady(player2, new TatteredRatter());
        harness.setHand(player1, List.of(new RoyalTreatment()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("A second Royal Role replaces the first rather than stacking its bonus")
    void secondRoleReplacesFirst() {
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        castRoyalTreatment(target);
        Permanent firstRole = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> target.getId().equals(permanent.getAttachedTo()))
                .findFirst().orElseThrow();

        castRoyalTreatment(target);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstRole.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> target.getId().equals(permanent.getAttachedTo())))
                .hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("An illegal target prevents both hexproof and Role creation")
    void removedTargetCreatesNoRole() {
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        harness.setHand(player1, List.of(new RoyalTreatment()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Royal Treatment");
    }

    @Test
    @DisplayName("The Role's ward counters an unpaid opposing spell after hexproof expires")
    void royalRoleCountersUnpaidSpell() {
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        castRoyalTreatment(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new RatOut()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Rat Out");
    }

    @Test
    @DisplayName("Paying ward allows the opposing spell to resolve")
    void payingRoyalWardAllowsSpell() {
        Permanent target = addCreatureReady(player1, new TatteredRatter());
        castRoyalTreatment(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new RatOut()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Rat");
    }

    private void castRoyalTreatment(Permanent target) {
        harness.setHand(player1, List.of(new RoyalTreatment()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
