package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({FleetingDistraction.class, MoorlandInquisitor.class, Cloudshift.class})
class FleetingDistractionTest extends BaseCardTest {

    private void setupBearAndSpell() {
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new FleetingDistraction()));
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    @Test
    @DisplayName("Resolving gives -1/-0 to target creature")
    void resolvingGivesMinusOneMinusZero() {
        setupBearAndSpell();
        UUID bearId = harness.getPermanentId(player2, "Moorland Inquisitor");

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving draws a card for the caster")
    void resolvingDrawsACard() {
        setupBearAndSpell();
        harness.setLibrary(player1, List.of(new MoorlandInquisitor()));
        UUID bearId = harness.getPermanentId(player2, "Moorland Inquisitor");

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Debuff wears off at end of turn")
    void debuffWearsOff() {
        setupBearAndSpell();
        UUID bearId = harness.getPermanentId(player2, "Moorland Inquisitor");

        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot cast with an invalid target")
    void cannotCastWithInvalidTarget() {
        setupBearAndSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    @Test
    @DisplayName("An own creature can be targeted and the caster draws")
    void canTargetOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new FleetingDistraction()));
        harness.setLibrary(player1, List.of(new MoorlandInquisitor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        harness.assertInHand(player1, "Moorland Inquisitor");
    }

    @Test
    @DisplayName("Does not draw when the target leaves and returns before resolution")
    void doesNotDrawWhenTargetIsBlinked() {
        setupBearAndSpell();
        harness.setLibrary(player1, List.of(new MoorlandInquisitor()));
        UUID targetId = harness.getPermanentId(player2, "Moorland Inquisitor");
        harness.setHand(player2, List.of(new Cloudshift()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Fleeting Distraction");
        Permanent returnedCreature = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(returnedCreature.getId()).isNotEqualTo(targetId);
        assertThat(returnedCreature.getEffectivePower()).isEqualTo(2);
        assertThat(returnedCreature.getEffectiveToughness()).isEqualTo(2);
    }
}
