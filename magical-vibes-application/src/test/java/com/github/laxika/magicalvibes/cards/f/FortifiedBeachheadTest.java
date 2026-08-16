package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FortifiedBeachheadTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Soldier and cannot reveal one")
    void entersTappedWithoutSoldier() {
        playLand(new FortifiedBeachhead());

        assertThat(findPermanent(player1, "Fortified Beachhead").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Soldier")
    void entersUntappedWithControlledSoldier() {
        harness.addToBattlefield(player1, new YotianSoldier());

        playLand(new FortifiedBeachhead());

        assertThat(findPermanent(player1, "Fortified Beachhead").isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Revealing a Soldier lets it enter untapped when you control no Soldier")
    void entersUntappedWhenRevealingSoldier() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FortifiedBeachhead(), new YotianSoldier()));

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Fortified Beachhead").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping for white or blue mana produces the chosen color")
    void producesWhiteOrBlueMana() {
        addReadyLand();
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);

        addReadyLand();
        harness.activateAbility(player1, 1, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Soldier pump affects only Soldiers until end of turn")
    void pumpsSoldiersUntilEndOfTurn() {
        Permanent land = addReadyLand();
        Permanent soldier = addCreatureReady(player1, new YotianSoldier());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int soldierPower = gqs.getEffectivePower(gd, soldier);
        int soldierToughness = gqs.getEffectiveToughness(gd, soldier);
        int bearPower = gqs.getEffectivePower(gd, bear);
        int bearToughness = gqs.getEffectiveToughness(gd, bear);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(soldierPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(soldierToughness + 1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPower);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(bearToughness);
    }

    private void playLand(com.github.laxika.magicalvibes.model.Card land) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyLand() {
        Permanent land = new Permanent(new FortifiedBeachhead());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(land);
        return land;
    }
}
