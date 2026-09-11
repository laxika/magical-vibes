package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({StolenStarkTech.class, GrizzlyBears.class})
class StolenStarkTechTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature you control and grants it indestructible")
    void entersAttachedAndGrantsIndestructible() {
        Permanent creature = addCreatureReady(player1);
        castStolenStarkTech(creature);

        Permanent tech = findPermanent(player1, "Stolen Stark Tech");
        assertThat(tech.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB indestructible expires at end of turn while the equipment bonus remains")
    void etbIndestructibleExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1);
        castStolenStarkTech(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(findPermanent(player1, "Stolen Stark Tech").getAttachedTo())
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip moves Stolen Stark Tech to another creature you control")
    void equipMovesToAnotherCreature() {
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        Permanent tech = harness.addToBattlefieldAndReturn(player1, new StolenStarkTech());
        tech.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(tech.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature when entering")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2);
        harness.setHand(player1, List.of(new StolenStarkTech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash allows casting Stolen Stark Tech during an opponent's combat")
    void flashAllowsCastingDuringOpponentsCombat() {
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new StolenStarkTech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castArtifact(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private void castStolenStarkTech(Permanent target) {
        harness.setHand(player1, List.of(new StolenStarkTech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
