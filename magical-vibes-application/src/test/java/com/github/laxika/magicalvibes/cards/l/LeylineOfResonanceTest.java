package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CrumbAndGetIt;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfResonance.class, CrumbAndGetIt.class, DualShot.class, GiantGrowth.class, GrizzlyBears.class, LightningBolt.class})
class LeylineOfResonanceTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfResonance()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData()
                .interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        openingHarness.assertOnBattlefield(openingHarness.getPlayer1(), "Leyline of Resonance");
    }

    @Test
    @DisplayName("Copies an instant targeting a single creature you control")
    void copiesSpellTargetingSingleCreatureYouControl() {
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(6);
        assertThat(target.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not copy a spell targeting an opponent's creature")
    void doesNotCopySpellTargetingOpponentsCreature() {
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not copy a spell with more than one target")
    void doesNotCopySpellWithMoreThanOneTarget() {
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(firstTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not copy a spell targeting a player")
    void doesNotCopySpellTargetingPlayer() {
        harness.addToBattlefield(player1, new LeylineOfResonance());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void mayDeclineOpeningHandPlacement() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfResonance()));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        openingHarness.assertNotOnBattlefield(openingHarness.getPlayer1(), "Leyline of Resonance");
        openingHarness.assertInHand(openingHarness.getPlayer1(), "Leyline of Resonance");
    }

    @Test
    void copyMayTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        resolveAllTriggers();

        assertThat(originalTarget.getPowerModifier()).isEqualTo(3);
        assertThat(originalTarget.getToughnessModifier()).isEqualTo(3);
        assertThat(newTarget.getPowerModifier()).isEqualTo(3);
        assertThat(newTarget.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    void doesNotCopyOpponentsSpellTargetingYourCreature() {
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    void copyPreservesPromisedGift() {
        harness.addToBattlefield(player1, new LeylineOfResonance());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrumbAndGetIt()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantWithGift(player1, 0, target.getId(), true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        assertThat(countPermanents(player2, "Food")).isEqualTo(2);
    }
}
