package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GlassCasket;
import com.github.laxika.magicalvibes.cards.f.Flutterfox;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.r.RevengeOfRavens;
import com.github.laxika.magicalvibes.cards.t.TheGreatHenge;
import com.github.laxika.magicalvibes.cards.t.TrappedInTheTower;
import com.github.laxika.magicalvibes.cards.c.CrystalSlipper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DanceOfTheManse.class, GlassCasket.class, CrystalSlipper.class,
        TrappedInTheTower.class, Flutterfox.class, TheGreatHenge.class,
        Gingerbrute.class, RevengeOfRavens.class})
class DanceOfTheManseTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to X eligible artifact and non-Aura enchantment cards")
    void returnsUpToXEligibleCards() {
        Card glassCasket = new GlassCasket();
        Card crystalSlipper = new CrystalSlipper();
        Card aura = new TrappedInTheTower();
        Card creature = new Flutterfox();
        harness.setGraveyard(player1, List.of(glassCasket, crystalSlipper, aura, creature));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(glassCasket.getId(), crystalSlipper.getId());

        harness.handleMultipleCardsChosen(player1, List.of(glassCasket.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glass Casket");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(crystalSlipper, aura, creature);
    }

    @Test
    @DisplayName("Makes returned permanents 4/4 creatures when X is at least 6")
    void animatesReturnedPermanentsForLargeX() {
        Card glassCasket = new GlassCasket();
        Card crystalSlipper = new CrystalSlipper();
        Card expensiveArtifact = new TheGreatHenge();
        Card aura = new TrappedInTheTower();
        harness.setGraveyard(player1, List.of(glassCasket, crystalSlipper, expensiveArtifact, aura));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 6);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(glassCasket.getId(), crystalSlipper.getId());
        harness.handleMultipleCardsChosen(player1,
                List.of(glassCasket.getId(), crystalSlipper.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Glass Casket"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Glass Casket"))).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Crystal Slipper"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Crystal Slipper"))).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expensiveArtifact, aura);
    }

    @Test
    @DisplayName("Returns non-Aura enchantments and artifact creatures without animating at X = 5")
    void returnsEnchantmentAndArtifactCreatureBelowAnimationThreshold() {
        Card enchantment = new RevengeOfRavens();
        Card artifactCreature = new Gingerbrute();
        Card opposingArtifact = new CrystalSlipper();
        harness.setGraveyard(player1, List.of(enchantment, artifactCreature));
        harness.setGraveyard(player2, List.of(opposingArtifact));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 5);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(enchantment.getId(), artifactCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId(), artifactCreature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Revenge of Ravens");
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Revenge of Ravens"))).isFalse();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Gingerbrute"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Gingerbrute"))).isEqualTo(1);
        harness.assertInGraveyard(player2, "Crystal Slipper");
    }

    @Test
    @DisplayName("Sets artifact creatures and non-Aura enchantments to 4/4 at X = 6")
    void animatesArtifactCreatureAndEnchantment() {
        Card enchantment = new RevengeOfRavens();
        Card artifactCreature = new Gingerbrute();
        harness.setGraveyard(player1, List.of(enchantment, artifactCreature));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 6);
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId(), artifactCreature.getId()));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, findPermanent(player1, "Revenge of Ravens"))).isTrue();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Revenge of Ravens"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Revenge of Ravens"))).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Gingerbrute"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Gingerbrute"))).isEqualTo(4);
    }

    @Test
    @DisplayName("Can be cast with X = 0 without returning anything")
    void zeroXReturnsNothing() {
        harness.setGraveyard(player1, List.of(new CrystalSlipper()));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Crystal Slipper");
        harness.assertInGraveyard(player1, "Crystal Slipper");
        harness.assertInGraveyard(player1, "Dance of the Manse");
    }

    @Test
    @DisplayName("Can choose zero targets even when eligible cards exist")
    void mayChooseNoTargets() {
        harness.setGraveyard(player1, List.of(new CrystalSlipper()));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 6);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Crystal Slipper");
        harness.assertInGraveyard(player1, "Crystal Slipper");
        harness.assertInGraveyard(player1, "Dance of the Manse");
    }

    @Test
    @DisplayName("Animated artifacts enter as creatures and trigger The Great Henge")
    void animatedArtifactTriggersCreatureEntryAbility() {
        Card artifact = new CrystalSlipper();
        harness.addToBattlefield(player1, new TheGreatHenge());
        harness.setLibrary(player1, List.of(new Flutterfox()));
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 6);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Crystal Slipper").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Crystal Slipper"))).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Crystal Slipper"))).isEqualTo(5);
        harness.assertInHand(player1, "Flutterfox");
    }

    @Test
    @DisplayName("Does not animate a target that entered the battlefield before resolution")
    void doesNotAnimateCardReturnedByAnotherEffect() {
        Card movedArtifact = new CrystalSlipper();
        Card validTarget = new RevengeOfRavens();
        harness.setGraveyard(player1, List.of(movedArtifact, validTarget));
        harness.setHand(player1, List.of(new DanceOfTheManse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 6);
        harness.handleMultipleCardsChosen(player1, List.of(movedArtifact.getId(), validTarget.getId()));
        harness.setGraveyard(player1, List.of(validTarget));
        harness.addToBattlefield(player1, movedArtifact);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, findPermanent(player1, "Crystal Slipper"))).isFalse();
        assertThat(gqs.isCreature(gd, findPermanent(player1, "Revenge of Ravens"))).isTrue();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Revenge of Ravens"))).isEqualTo(4);
    }
}
