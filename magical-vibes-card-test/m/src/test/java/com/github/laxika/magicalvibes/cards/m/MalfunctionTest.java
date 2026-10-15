package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AppetiteForTheUnnatural;
import com.github.laxika.magicalvibes.cards.b.BlossomingDefense;
import com.github.laxika.magicalvibes.cards.d.DramaticReversal;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WilyBandar;
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

@CardUsed({Malfunction.class, FountainOfYouth.class, GrizzlyBears.class, Forest.class,
        WilyBandar.class, BlossomingDefense.class, DramaticReversal.class, AppetiteForTheUnnatural.class})
class MalfunctionTest extends BaseCardTest {

    @Test
    @DisplayName("Malfunction taps and prevents the enchanted artifact from untapping")
    void tapsEnchantedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castMalfunction(artifact);
        advanceToNextTurn(player1);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Malfunction prevents the enchanted creature from untapping")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        castMalfunction(creature);
        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Malfunction cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Malfunction()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Malfunction taps an initially untapped creature only when its entry trigger resolves")
    void tapsCreatureWhenEntryTriggerResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WilyBandar());
        harness.setHand(player2, List.of(new BlossomingDefense()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Malfunction()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(findPermanent(player1, "Malfunction").getAttachedTo()).isEqualTo(creature.getId());

        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hexproof gained after Malfunction enters does not stop its nontargeted tap trigger")
    void entryTriggerStillTapsCreatureThatGainsHexproof() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WilyBandar());
        harness.setHand(player2, List.of(new BlossomingDefense()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new Malfunction()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();

        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Malfunction").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Malfunction allows untapping by a spell and does not continuously tap its host")
    void enchantedCreatureCanBeUntappedBySpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WilyBandar());
        castMalfunction(creature);
        assertThat(creature.isTapped()).isTrue();

        harness.setHand(player1, List.of(new DramaticReversal()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(creature.isTapped()).isFalse();
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the enchanted permanent stays tapped, and destroying Malfunction ends the restriction")
    void restrictionEndsWhenAuraLeavesBattlefield() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new WilyBandar());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new WilyBandar());
        other.tap();
        castMalfunction(enchanted);

        harness.performUntapStep(player2);
        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();

        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player2, List.of(new AppetiteForTheUnnatural()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Malfunction").getId());
        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Malfunction")).isEmpty();
    }

    private void castMalfunction(Permanent target) {
        harness.setHand(player1, List.of(new Malfunction()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(currentActivePlayer == player1 ? player2 : player1, TurnStep.UPKEEP);
    }
}
