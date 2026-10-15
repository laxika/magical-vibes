package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrMadisonLi.class, GrizzlyBears.class, Ornithopter.class, Shock.class, SolRing.class})
class DrMadisonLiTest extends BaseCardTest {

    @Test
    void gainsEnergyWhenControllerCastsArtifactSpell() {
        harness.addToBattlefield(player1, new DrMadisonLi());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void paysEnergyToBoostTargetCreatureAndGrantKeywords() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 0,
                null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    void paysThreeEnergyToDrawACard() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        harness.setLibrary(player1, List.of(new Shock()));
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 1,
                null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Shock");
    }

    @Test
    void returnsTargetArtifactFromGraveyardTapped() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        gd.playerEnergyCounters.put(player1.getId(), 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 2,
                null, artifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(artifact);
        Permanent returned = findPermanent(player1, "Ornithopter");
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonArtifactCardInGraveyard() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        gd.playerEnergyCounters.put(player1.getId(), 5);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 2,
                null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void energyTriggerResolvesBeforeArtifactSpell() {
        harness.addToBattlefield(player1, new DrMadisonLi());
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castArtifact(player1, 0);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    void opponentsArtifactSpellDoesNotGiveEnergy() {
        harness.addToBattlefield(player1, new DrMadisonLi());
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void nonArtifactSpellDoesNotGiveEnergy() {
        harness.addToBattlefield(player1, new DrMadisonLi());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void canBoostOpponentsCreatureAndEffectsExpireAtEndOfTurn() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 0,
                null, bears.getId());

        assertThat(doctor.isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    void eachAbilityRequiresEnoughEnergy() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        int[] costs = {1, 3, 5};

        for (int abilityIndex = 0; abilityIndex < costs.length; abilityIndex++) {
            int index = abilityIndex;
            gd.playerEnergyCounters.put(player1.getId(), costs[index] - 1);

            assertThatThrownBy(() -> harness.activateAbility(player1,
                    gd.playerBattlefields.get(player1.getId()).indexOf(doctor), index, null,
                    index == 0 ? bears.getId() : index == 2 ? artifact.getId() : null,
                    index == 2 ? Zone.GRAVEYARD : Zone.BATTLEFIELD))
                    .isInstanceOf(IllegalStateException.class);

            assertThat(doctor.isTapped()).isFalse();
            assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(costs[index] - 1);
        }
    }

    @Test
    void summoningSickDoctorCannotActivateTapAbility() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new DrMadisonLi());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(doctor.isTapped()).isFalse();
    }

    @Test
    void cannotReturnArtifactFromOpponentsGraveyard() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Card artifact = new Ornithopter();
        harness.setGraveyard(player2, List.of(artifact));
        gd.playerEnergyCounters.put(player1.getId(), 5);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 2,
                null, artifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsNonCreatureArtifactTappedWithoutTriggeringCastAbility() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Card artifact = new SolRing();
        harness.setGraveyard(player1, List.of(artifact));
        gd.playerEnergyCounters.put(player1.getId(), 6);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 2,
                null, artifact.getId(), Zone.GRAVEYARD);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(doctor.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Sol Ring");
        assertThat(findPermanent(player1, "Sol Ring").isTapped()).isTrue();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void removedGraveyardTargetIsNotReturnedAndEnergyIsNotRefunded() {
        Permanent doctor = addCreatureReady(player1, new DrMadisonLi());
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        gd.playerEnergyCounters.put(player1.getId(), 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(doctor), 2,
                null, artifact.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(artifact));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gd.findExiledCard(artifact.getId())).isNotNull();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(doctor.isTapped()).isTrue();
    }
}
