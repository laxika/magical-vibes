package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AnkhOfMishra;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxJasper;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DisruptiveStormbrood.class, AnkhOfMishra.class, CrawWurm.class, GrizzlyBears.class, RuleOfLaw.class, MoxJasper.class})
class DisruptiveStormbroodTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a target artifact")
    void etbDestroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AnkhOfMishra());

        castCreature(List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Ankh of Mishra");
        harness.assertOnBattlefield(player1, "Disruptive Stormbrood");
    }

    @Test
    @DisplayName("ETB destroys a target enchantment")
    void etbDestroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new RuleOfLaw());

        castCreature(List.of(enchantment.getId()));

        harness.assertInGraveyard(player2, "Rule of Law");
    }

    @Test
    @DisplayName("ETB can resolve without a target")
    void etbCanResolveWithoutTarget() {
        castCreature(List.of());

        harness.assertOnBattlefield(player1, "Disruptive Stormbrood");
    }

    @Test
    @DisplayName("Omen destroys a creature with power 3 or less and shuffles the card into its owner's library")
    void omenDestroysSmallCreatureAndShuffles() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DisruptiveStormbrood card = new DisruptiveStormbrood();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Disruptive Stormbrood");
        org.assertj.core.api.Assertions.assertThat(gd.playerDecks.get(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Omen cannot target a creature with power greater than 3")
    void omenRejectsLargeCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.setHand(player1, List.of(new DisruptiveStormbrood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    void etbCanDeclineTargetEvenWhenArtifactExists() {
        harness.addToBattlefield(player2, new MoxJasper());

        castCreature(List.of());

        harness.assertOnBattlefield(player2, "Mox Jasper");
        harness.assertOnBattlefield(player1, "Disruptive Stormbrood");
    }

    @Test
    void etbCanDestroyControllersArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MoxJasper());

        castCreature(List.of(artifact.getId()));

        harness.assertInGraveyard(player1, "Mox Jasper");
    }

    @Test
    void omenDestroysCreatureWithExactlyThreePowerAndAddsOnlyOneCardToLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DisruptiveStormbrood());
        DisruptiveStormbrood card = new DisruptiveStormbrood();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Disruptive Stormbrood");
        harness.assertNotInGraveyard(player1, "Disruptive Stormbrood");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(card.getId());
    }

    @Test
    void omenDoesNotResolveOrShuffleWhenTargetsPowerBecomesTooHigh() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DisruptiveStormbrood());
        DisruptiveStormbrood card = new DisruptiveStormbrood();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, creature.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Disruptive Stormbrood");
        harness.assertInGraveyard(player1, "Disruptive Stormbrood");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void omenCannotTargetANoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MoxJasper());
        harness.setHand(player1, List.of(new DisruptiveStormbrood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCreature(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new DisruptiveStormbrood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
