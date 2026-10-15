package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantCrab;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scuttletide.class, GrizzlyBears.class, GiantCrab.class, Plains.class, Shock.class,
        LeoninScimitar.class, Pacifism.class, OrnithopterOfParadise.class, Opalescence.class,
        MaskwoodNexus.class})
class ScuttletideTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {1} and discards a card to create a 0/3 blue Crab")
    void createsCrabAfterDiscarding() {
        Permanent scuttletide = castScuttletide();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scuttletide),
                0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Crab");
        assertThat(crab.getCard().getPower()).isZero();
        assertThat(crab.getCard().getToughness()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Delirium gives Crabs you control +1/+1")
    void deliriumBoostsOwnCrabsOnly() {
        harness.setGraveyard(player1, List.of(
                new Plains(), new Shock(), new LeoninScimitar(), new Pacifism()));
        Permanent opponentCrab = harness.addToBattlefieldAndReturn(player2, new GiantCrab());
        int opponentCrabPower = gqs.getEffectivePower(gd, opponentCrab);
        int opponentCrabToughness = gqs.getEffectiveToughness(gd, opponentCrab);
        Permanent scuttletide = castScuttletide();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scuttletide),
                0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent crab = findPermanent(player1, "Crab");
        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCrab)).isEqualTo(opponentCrabPower);
        assertThat(gqs.getEffectiveToughness(gd, opponentCrab)).isEqualTo(opponentCrabToughness);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        Permanent scuttletide = castScuttletide();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scuttletide), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castScuttletide() {
        harness.castFromHand(player1, new Scuttletide(), "{1}{U}");
        harness.passBothPriorities();
        return findPermanent(player1, "Scuttletide");
    }

    @Test
    void deliriumTracksGraveyardChangesAndDoesNotBoostNonCrabs() {
        castScuttletide();
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new GiantCrab());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Plains(), new Shock(), new LeoninScimitar()));
        harness.setGraveyard(player2, List.of(new Pacifism()));

        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(3);

        harness.setGraveyard(player1, List.of(
                new Plains(), new Shock(), new LeoninScimitar(), new Pacifism()));
        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new Plains(), new Shock(), new LeoninScimitar()));
        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(3);
    }

    @Test
    void deliriumCountsMultipleTypesOnOneCardButNotDuplicateTypes() {
        castScuttletide();
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new GiantCrab());
        harness.setGraveyard(player1, List.of(
                new Plains(), new Shock(), new OrnithopterOfParadise()));

        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(4);

        harness.setGraveyard(player1, List.of(
                new Plains(), new Shock(), new Shock(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(3);
    }

    @Test
    void discardingLandPaysCostImmediatelyAndEnablesDelirium() {
        Permanent scuttletide = castScuttletide();
        Permanent crab = harness.addToBattlefieldAndReturn(player1, new GiantCrab());
        harness.setGraveyard(player1, List.of(new Shock(), new LeoninScimitar(), new Pacifism()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scuttletide),
                0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Plains");
        harness.assertNotInHand(player1, "Plains");
        assertThat(countPermanents(player1, "Crab")).isZero();
        assertThat(gqs.getEffectivePower(gd, crab)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crab)).isEqualTo(4);

        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Crab");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
    }

    @Test
    void canActivateRepeatedlyWithoutTapping() {
        Permanent scuttletide = castScuttletide();
        harness.setHand(player1, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scuttletide),
                    0, null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }

        assertThat(countPermanents(player1, "Crab")).isEqualTo(2);
        assertThat(scuttletide.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void animatedScuttletideThatIsACrabReceivesItsOwnDeliriumBonus() {
        Permanent scuttletide = castScuttletide();
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.setGraveyard(player1, List.of(
                new Plains(), new Shock(), new LeoninScimitar(), new Pacifism()));

        assertThat(gqs.getEffectivePower(gd, scuttletide)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scuttletide)).isEqualTo(3);
    }
}
