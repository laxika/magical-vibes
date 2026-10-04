package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.c.CompassGnome;
import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.cards.s.SovereignsMacuahuitl;
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

@CardUsed({IdolOfTheDeepKing.class, SovereignsMacuahuitl.class, GrizzlyBears.class, Millstone.class,
        CompassGnome.class, PanickedAltisaur.class})
class IdolOfTheDeepKingTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Idol of the Deep King deals 2 damage to the chosen player")
    void enterTheBattlefieldDealsDamageToPlayer() {
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new IdolOfTheDeepKing()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Craft returns Idol of the Deep King transformed and attaches the Equipment to a creature")
    void craftsIntoSovereignsMacuahuitlAndAttachesIt() {
        Permanent idol = harness.addToBattlefieldAndReturn(player1, new IdolOfTheDeepKing());
        harness.addToBattlefield(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        Permanent equipment = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SovereignsMacuahuitl)
                .findFirst().orElseThrow();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(idol);
        assertThat(equipment.isTransformed()).isTrue();
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    void enteringIdolCanKillAnOpposingCreature() {
        harness.addToBattlefield(player2, new CompassGnome());
        harness.setHand(player1, List.of(new IdolOfTheDeepKing()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castArtifact(player1, 0, harness.getPermanentId(player2, "Compass Gnome"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Compass Gnome");
        harness.assertInGraveyard(player2, "Compass Gnome");
    }

    @Test
    void craftsWithAnArtifactFromGraveyardWithoutAnyCreatureToAttachTo() {
        Permanent idol = harness.addToBattlefieldAndReturn(player1, new IdolOfTheDeepKing());
        CompassGnome material = new CompassGnome();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(idol);
        assertThat(gd.findExiledCard(idol.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Compass Gnome");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sovereign's Macuahuitl");
        Permanent equipment = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(equipment.isTransformed()).isTrue();
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.findExiledCard(idol.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCraftUsingOnlyItselfOrAnOpponentsArtifact() {
        Permanent idol = harness.addToBattlefieldAndReturn(player1, new IdolOfTheDeepKing());
        harness.addToBattlefield(player2, new CompassGnome());
        harness.addToBattlefield(player1, new PanickedAltisaur());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(idol);
        harness.assertOnBattlefield(player2, "Compass Gnome");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftCannotBeActivatedOutsideMainPhase() {
        harness.addToBattlefield(player1, new IdolOfTheDeepKing());
        harness.setGraveyard(player1, List.of(new CompassGnome()));
        harness.addMana(player1, ManaColor.RED, 3);
        gd.currentStep = TurnStep.UPKEEP;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Idol of the Deep King");
        harness.assertInGraveyard(player1, "Compass Gnome");
    }

    @Test
    void equipMovesPowerBonusWithoutChangingToughness() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SovereignsMacuahuitl());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SovereignsMacuahuitl());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PanickedAltisaur());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    void flashAllowsCastingDuringUpkeepAndDamageCanTargetItsController() {
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new IdolOfTheDeepKing()));
        harness.addMana(player1, ManaColor.RED, 3);
        gd.currentStep = TurnStep.UPKEEP;

        harness.castArtifact(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Idol of the Deep King");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void nonartifactGraveyardCardCannotBeCraftMaterial() {
        harness.addToBattlefield(player1, new IdolOfTheDeepKing());
        harness.setGraveyard(player1, List.of(new PanickedAltisaur()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Idol of the Deep King");
        harness.assertInGraveyard(player1, "Panicked Altisaur");
    }

    @Test
    void equipCannotBeActivatedOutsideMainPhase() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SovereignsMacuahuitl());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.currentStep = TurnStep.UPKEEP;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(equipment.getAttachedTo()).isNull();
    }
}
