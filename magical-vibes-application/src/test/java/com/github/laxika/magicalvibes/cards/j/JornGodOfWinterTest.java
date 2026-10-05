package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.cards.b.BorealOutrider;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.k.KaldringTheRimestaff;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({JornGodOfWinter.class, KaldringTheRimestaff.class, Forest.class,
        SnowCoveredForest.class, BorealOutrider.class, FrostBite.class})
class JornGodOfWinterTest extends BaseCardTest {

    @Test
    void attackingUntapsSnowPermanentsButNotOtherPermanents() {
        addCreatureReady(player1, new JornGodOfWinter());
        Permanent snowForest = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        snowForest.tap();
        forest.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(snowForest.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    void targetedSnowLandPlayedFromGraveyardEntersTapped() {
        Permanent kaldring = addKaldring();
        Card snowForest = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(snowForest));

        harness.activateAbility(player1, 0, null, snowForest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.playGraveyardLand(player1, 0);

        Permanent entered = findPermanent(player1, "Snow-Covered Forest");
        assertThat(kaldring.isTapped()).isTrue();
        assertThat(entered.isTapped()).isTrue();
    }

    @Test
    void targetedSnowPermanentSpellEntersTapped() {
        addKaldring();
        Card snowCreature = new BorealOutrider();
        harness.setGraveyard(player1, List.of(snowCreature));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, snowCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent entered = findPermanent(player1, "Boreal Outrider");
        assertThat(entered.isTapped()).isTrue();
    }

    @Test
    void abilityCannotTargetNonSnowPermanentCard() {
        addKaldring();
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCastJornFaceAndAttackToUntapItself() {
        harness.setHand(player1, List.of(new JornGodOfWinter()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        Permanent jorn = findPermanent(player1, "Jorn, God of Winter");
        jorn.setSummoningSick(false);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(jorn.isTapped()).isFalse();
    }

    @Test
    void canCastKaldringFaceAndUseItsAbilityImmediately() {
        harness.setHand(player1, List.of(new JornGodOfWinter()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card snowForest = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(snowForest));

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, snowForest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.playGraveyardLand(player1, 0);

        assertThat(findPermanent(player1, "Kaldring, the Rimestaff").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Snow-Covered Forest").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Jorn, God of Winter");
    }

    @Test
    void attackingUntapsSnowCreaturesAndArtifactsButNotOpponentsSnowPermanents() {
        Permanent jorn = addCreatureReady(player1, new JornGodOfWinter());
        Permanent outrider = harness.addToBattlefieldAndReturn(player1, new BorealOutrider());
        Permanent kaldring = addKaldring();
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        outrider.tap();
        kaldring.tap();
        opposingLand.tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(jorn.isTapped()).isFalse();
        assertThat(outrider.isTapped()).isFalse();
        assertThat(kaldring.isTapped()).isFalse();
        assertThat(opposingLand.isTapped()).isTrue();
    }

    @Test
    void abilityCannotTargetOpponentsSnowPermanentCard() {
        addKaldring();
        Card snowForest = new SnowCoveredForest();
        harness.setGraveyard(player2, List.of(snowForest));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, snowForest.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityCannotTargetSnowInstantCard() {
        addKaldring();
        Card frostBite = new FrostBite();
        harness.setGraveyard(player1, List.of(frostBite));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, frostBite.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionOnlyAllowsPlayingTheTargetedCard() {
        addKaldring();
        Card targetedLand = new SnowCoveredForest();
        Card otherLand = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(targetedLand, otherLand));

        harness.activateAbility(player1, 0, null, targetedLand.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.playGraveyardLand(player1, 0);
        assertThat(findPermanent(player1, "Snow-Covered Forest").isTapped()).isTrue();
    }

    @Test
    void castingTargetedSnowCreatureStillRequiresMana() {
        addKaldring();
        Card snowCreature = new BorealOutrider();
        harness.setGraveyard(player1, List.of(snowCreature));

        harness.activateAbility(player1, 0, null, snowCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Boreal Outrider").isTapped()).isTrue();
    }

    @Test
    void permissionDoesNotOverrideCreatureTiming() {
        addKaldring();
        Card snowCreature = new BorealOutrider();
        harness.setGraveyard(player1, List.of(snowCreature));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, snowCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetedJornCardCanBeCastAsKaldringAfterTheSourceLeaves() {
        Permanent source = addKaldring();
        Card jorn = new JornGodOfWinter();
        harness.setGraveyard(player1, List.of(jorn));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, jorn.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToHand(gd, source);
        harness.ensurePriority(player1);
        gs.playFlashbackSpell(gd, player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Kaldring, the Rimestaff").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Jorn, God of Winter");
        harness.assertNotInGraveyard(player1, "Jorn, God of Winter");
    }

    @Test
    void permissionDoesNotGrantAnAdditionalLandPlay() {
        addKaldring();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Card snowForest = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(snowForest));

        harness.activateAbility(player1, 0, null, snowForest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Snow-Covered Forest");
    }

    @Test
    void unusedPermissionExpiresAtEndOfTurnAndLeavesTheCardInTheGraveyard() {
        addKaldring();
        Card snowForest = new SnowCoveredForest();
        harness.setGraveyard(player1, List.of(snowForest));

        harness.activateAbility(player1, 0, null, snowForest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Snow-Covered Forest");
    }

    private Permanent addKaldring() {
        return harness.addToBattlefieldAndReturn(player1, new KaldringTheRimestaff());
    }
}
