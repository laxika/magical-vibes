package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.s.SparkHarvest;
import com.github.laxika.magicalvibes.cards.t.TotallyLost;
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

@CardUsed({ObNixilissCruelty.class, AvatarOfMight.class, Forest.class, GrizzlyBears.class,
        PrimordialWurm.class, SparkHarvest.class, TotallyLost.class})
class ObNixilissCrueltyTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -5/-5 until end of turn")
    void givesTargetCreatureMinusFiveMinusFive() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new ObNixilissCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiles a creature killed by the reduction")
    void exilesCreatureKilledByReduction() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ObNixilissCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ObNixilissCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void exilesSurvivingCreatureDestroyedLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new ObNixilissCruelty(), new SparkHarvest()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Primordial Wurm");
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        harness.assertNotInGraveyard(player2, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void reductionAndExileReplacementExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new ObNixilissCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        harness.setHand(player2, List.of(new SparkHarvest()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castSorceryWithSacrifice(player2, 0, target.getId(), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        harness.assertInGraveyard(player2, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void doesNotExileTargetMovedToLibraryInsteadOfDying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new ObNixilissCruelty(), new TotallyLost()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId())
                .isEqualTo(target.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void doesNothingWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new ObNixilissCruelty()));
        harness.setHand(player2, List.of(new TotallyLost()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId())
                .isEqualTo(target.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertInGraveyard(player1, "Ob Nixilis's Cruelty");
    }
}
