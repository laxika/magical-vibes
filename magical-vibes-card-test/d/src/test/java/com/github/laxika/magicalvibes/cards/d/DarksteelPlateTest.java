package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarksteelPlate.class, PhyrexianRager.class, DivineOffering.class, GoForTheThroat.class})
class DarksteelPlateTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Darksteel Plate to target creature")
    void resolvingEquipAttaches() {
        Permanent plate = addReadyPlate(player1);
        Permanent creature = addCreatureReady(player1, new PhyrexianRager());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature has indestructible keyword")
    void equippedCreatureHasIndestructible() {
        Permanent creature = addCreatureReady(player1, new PhyrexianRager());
        Permanent plate = addReadyPlate(player1);
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses indestructible when Darksteel Plate is removed")
    void creatureLosesIndestructibleWhenPlateRemoved() {
        Permanent creature = addCreatureReady(player1, new PhyrexianRager());
        Permanent plate = addReadyPlate(player1);
        plate.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(plate);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Unequipped creatures do not get indestructible")
    void unequippedCreatureDoesNotGetIndestructible() {
        Permanent creature1 = addCreatureReady(player1, new PhyrexianRager());
        Permanent creature2 = addCreatureReady(player1, new PhyrexianRager());
        Permanent plate = addReadyPlate(player1);
        plate.setAttachedTo(creature1.getId());

        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Moving Darksteel Plate transfers indestructible to new creature")
    void reEquipTransfersIndestructible() {
        Permanent plate = addReadyPlate(player1);
        Permanent creature1 = addCreatureReady(player1, new PhyrexianRager());
        Permanent creature2 = addCreatureReady(player1, new PhyrexianRager());
        plate.setAttachedTo(creature1.getId());

        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(plate.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void equipRequiresTwoMana() {
        Permanent plate = addReadyPlate(player1);
        Permanent creature = addCreatureReady(player1, new PhyrexianRager());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(plate.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent plate = addReadyPlate(player1);
        Permanent creature = addCreatureReady(player2, new PhyrexianRager());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(plate.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedOutsideMainPhase() {
        Permanent plate = addReadyPlate(player1);
        Permanent creature = addCreatureReady(player1, new PhyrexianRager());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(plate.getAttachedTo()).isNull();
    }

    @Test
    void plateSurvivesArtifactDestruction() {
        addReadyPlate(player1);
        harness.setHand(player2, List.of(new DivineOffering()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0, gd.playerBattlefields.get(player1.getId()).getFirst().getId());

        harness.assertOnBattlefield(player1, "Darksteel Plate");
        harness.assertNotInGraveyard(player1, "Darksteel Plate");
    }

    @Test
    void equippedCreatureSurvivesDestruction() {
        Permanent creature = addCreatureReady(player1, new PhyrexianRager());
        addReadyPlate(player1).setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Phyrexian Rager");
        harness.assertNotInGraveyard(player1, "Phyrexian Rager");
    }

    @Test
    void lethalDamageKillsCreatureOncePlateIsDetached() {
        Permanent creature = addCreatureReady(player1, new PhyrexianRager());
        Permanent plate = addReadyPlate(player1);
        plate.setAttachedTo(creature.getId());
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Phyrexian Rager");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);

        plate.setAttachedTo(null);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Phyrexian Rager");
        harness.assertInGraveyard(player1, "Phyrexian Rager");
        harness.assertOnBattlefield(player1, "Darksteel Plate");
    }
    private Permanent addReadyPlate(Player player) {
        return harness.addToBattlefieldAndReturn(player, new DarksteelPlate());
    }
}
