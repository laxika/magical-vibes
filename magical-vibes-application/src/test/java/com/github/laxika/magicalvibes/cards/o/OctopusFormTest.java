package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.n.NorthPolePatrol;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OctopusForm.class, NorthPolePatrol.class})
class OctopusFormTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, boosts, and grants hexproof to target creature you control")
    void untapsBoostsAndGrantsHexproof() {
        Permanent target = addTappedCreature(player1);

        castOctopusForm(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The boost and hexproof expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = addTappedCreature(player1);

        castOctopusForm(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NorthPolePatrol());
        harness.setHand(player1, List.of(new OctopusForm()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("An untapped creature still receives the boost and hexproof")
    void affectsUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NorthPolePatrol());

        castOctopusForm(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("You can target your own hexproof creature again and the boosts stack")
    void canTargetOwnHexproofCreatureAgain() {
        Permanent target = addTappedCreature(player1);

        castOctopusForm(target);
        castOctopusForm(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("No effects apply if an opponent gains control of the target before resolution")
    void fizzlesWhenTargetChangesController() {
        Permanent target = addTappedCreature(player1);
        harness.setHand(player1, List.of(new OctopusForm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
        harness.assertInGraveyard(player1, "Octopus Form");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Granted hexproof prevents an opponent from targeting the creature with an ability")
    void hexproofStopsOpponentAbility() {
        Permanent target = addTappedCreature(player1);
        addCreatureReady(player2, new NorthPolePatrol());
        castOctopusForm(target);
        harness.addMana(player2, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(target.isTapped()).isFalse();
    }

    private void castOctopusForm(Permanent target) {
        harness.setHand(player1, List.of(new OctopusForm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addTappedCreature(Player player) {
        Permanent permanent = addCreatureReady(player, new NorthPolePatrol());
        permanent.tap();
        return permanent;
    }
}
