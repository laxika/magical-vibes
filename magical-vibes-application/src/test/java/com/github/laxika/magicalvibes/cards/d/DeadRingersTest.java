package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BattlefieldForge;
import com.github.laxika.magicalvibes.cards.c.CoastalDrake;
import com.github.laxika.magicalvibes.cards.e.EmblazonedGolem;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.m.MournfulZombie;
import com.github.laxika.magicalvibes.cards.s.SpectralLynx;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        DeadRingers.class, BattlefieldForge.class, CoastalDrake.class, EmblazonedGolem.class,
        GaeasSkyfolk.class, MournfulZombie.class, SpectralLynx.class
})
class DeadRingersTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys two nonblack creatures with exactly matching colors")
    void destroysCreaturesWithMatchingColors() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        castDeadRingers(List.of(first.getId(), second.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(first.getCard().getId()))
                .anyMatch(card -> card.getId().equals(second.getCard().getId()));
    }

    @Test
    @DisplayName("Does nothing when the targets have different colors")
    void doesNothingWhenColorsDiffer() {
        Permanent monoBlue = harness.addToBattlefieldAndReturn(player2, new CoastalDrake());
        Permanent blueGreen = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk());
        castDeadRingers(List.of(monoBlue.getId(), blueGreen.getId()));

        harness.assertOnBattlefield(player2, "Coastal Drake");
        harness.assertOnBattlefield(player2, "Gaea's Skyfolk");
    }

    @Test
    @DisplayName("Uses a departed target's last-known color and destroys the remaining target")
    void usesDepartedTargetsLastKnownColor() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        castDeadRingersOnStack(List.of(departed.getId(), remaining.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(remaining.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(remaining.getCard().getId()));
    }

    @Test
    @DisplayName("Uses the color of a target that becomes illegal before resolution")
    void usesColorOfTargetThatBecomesIllegal() {
        Permanent illegalTarget = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        castDeadRingersOnStack(List.of(illegalTarget.getId(), remaining.getId()));

        illegalTarget.getGrantedKeywords().add(Keyword.HEXPROOF);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(illegalTarget)
                .noneMatch(permanent -> permanent.getId().equals(remaining.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(remaining.getCard().getId()));
    }

    @Test
    @DisplayName("Remembers a departed target's changed color rather than its printed color")
    void remembersChangedColorOfDepartedTarget() {
        Permanent departed = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new CoastalDrake());
        castDeadRingersOnStack(List.of(departed.getId(), remaining.getId()));

        harness.inMutationScope(() -> {
            departed.setColorOverridden(true);
            departed.getTransientColors().add(CardColor.BLUE);
        });
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, departed));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Coastal Drake");
        harness.assertInGraveyard(player2, "Coastal Drake");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new MournfulZombie());
        Permanent nonblackCreature = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        prepareDeadRingers();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(
                blackCreature.getId(), nonblackCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonblack");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new BattlefieldForge());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        prepareDeadRingers();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys two colorless creatures")
    void destroysTwoColorlessCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new EmblazonedGolem());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new EmblazonedGolem());
        castDeadRingers(List.of(first.getId(), second.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(first.getCard().getId()))
                .anyMatch(card -> card.getId().equals(second.getCard().getId()));
    }

    @Test
    @DisplayName("Destroys two creatures with the same multicolor combination")
    void destroysTwoCreaturesWithMatchingMulticolorCombination() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk());
        castDeadRingers(List.of(first.getId(), second.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(first.getCard().getId()))
                .anyMatch(card -> card.getId().equals(second.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot be stopped by a regeneration shield")
    void cannotBeRegenerated() {
        Permanent shielded = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        shielded.setRegenerationShield(1);
        castDeadRingers(List.of(shielded.getId(), other.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(shielded.getCard().getId()))
                .anyMatch(card -> card.getId().equals(other.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void cannotChooseSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SpectralLynx());
        prepareDeadRingers();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDeadRingersOnStack(List<UUID> targets) {
        prepareDeadRingers();
        harness.castSorcery(player1, 0, targets);
    }

    private void prepareDeadRingers() {
        harness.setHand(player1, List.of(new DeadRingers()));
        harness.addMana(player1, ManaColor.BLACK, 5);
    }

    private void castDeadRingers(List<UUID> targets) {
        prepareDeadRingers();
        harness.castAndResolveSorcery(player1, 0, targets);
    }
}
