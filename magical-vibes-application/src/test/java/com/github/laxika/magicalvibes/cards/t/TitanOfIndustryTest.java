package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GildedPinions;
import com.github.laxika.magicalvibes.cards.c.CivicGardener;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.h.HaloScarab;
import com.github.laxika.magicalvibes.cards.s.StimulusPackage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitanOfIndustry.class, GildedPinions.class, CivicGardener.class, HaloScarab.class, StimulusPackage.class})
class TitanOfIndustryTest extends BaseCardTest {

    @Test
    void choosesTokenAndTargetPlayerLifeGain() {
        castTitan();
        chooseModes("Target player gains 5 life.", "Create a 4/4 green Rhino Warrior creature token.");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(25);
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
    }

    @Test
    void choosesArtifactDestructionAndShieldCounter() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GildedPinions());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CivicGardener());

        castTitan();
        chooseModes("Destroy target artifact or enchantment.", "Put a shield counter on a creature you control.");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        harness.assertInGraveyard(player2, "Gilded Pinions");
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isOne();
    }

    @Test
    void shieldModeChoosesOnlyCreatureYouControlAtResolution() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CivicGardener());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CivicGardener());

        castTitan();
        chooseModes("Create a 4/4 green Rhino Warrior creature token.", "Put a shield counter on a creature you control.");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownCreature.getId()));

        assertThat(ownCreature.getCounterCount(CounterType.SHIELD)).isOne();
        assertThat(opponentCreature.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    void canPutShieldOnTheRhinoCreatedByTheSameAbility() {
        castTitan();
        chooseModes("Create a 4/4 green Rhino Warrior creature token.", "Put a shield counter on a creature you control.");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();
        Permanent rhino = findPermanents(player1, "Rhino Warrior").getFirst();
        harness.handleMultiplePermanentsChosen(player1, List.of(rhino.getId()));

        assertThat(rhino.getCounterCount(CounterType.SHIELD)).isOne();
    }

    @Test
    void destroysEnchantmentAndGainsLifeForController() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new StimulusPackage());
        castTitan();
        chooseModes("Destroy target artifact or enchantment.", "Target player gains 5 life.");
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stimulus Package");
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
    }

    @Test
    void resolvesModesInPrintedOrderEvenWhenChosenInReverseOrder() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new HaloScarab());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new CivicGardener());
        castTitan();
        chooseModes("Put a shield counter on a creature you control.", "Destroy target artifact or enchantment.");
        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Halo Scarab");
        harness.handleMultiplePermanentsChosen(player1, List.of(survivor.getId()));
        assertThat(survivor.getCounterCount(CounterType.SHIELD)).isOne();
    }

    private void castTitan() {
        harness.castFromHand(player1, new TitanOfIndustry(), "{4}{G}{G}{G}");
        harness.passBothPriorities();
    }

    private void chooseModes(String first, String second) {
        harness.handleListChoice(player1, first);
        harness.handleListChoice(player1, second);
    }
}
