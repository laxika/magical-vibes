package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KolodinTriumphCasterTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your Mounts and Vehicles haste")
    void givesOwnMountsAndVehiclesHaste() {
        Permanent ownMount = addCreatureReady(player1, new BrightfieldGlider());
        Permanent ownVehicle = addVehicleReady(player1);
        Permanent opposingMount = addCreatureReady(player2, new BrightfieldGlider());
        addCreatureReady(player1, new KolodinTriumphCaster());

        assertThat(gqs.hasKeyword(gd, ownMount, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownVehicle, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingMount, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Saddles a Mount when it enters until end of turn")
    void saddlesEnteringMount() {
        addCreatureReady(player1, new KolodinTriumphCaster());
        BrightfieldGlider mountCard = new BrightfieldGlider();
        harness.setHand(player1, List.of(mountCard));
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mount = findPermanentByCardId(mountCard.getId());
        assertThat(mount.isSaddled()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Makes an entering Vehicle an artifact creature until end of turn")
    void animatesEnteringVehicle() {
        addCreatureReady(player1, new KolodinTriumphCaster());
        DuskLegionDreadnought vehicleCard = new DuskLegionDreadnought();
        harness.setHand(player1, List.of(vehicleCard));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vehicle = findPermanentByCardId(vehicleCard.getId());
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.isArtifact(vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.isArtifact(vehicle)).isTrue();
    }

    private Permanent addVehicleReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent vehicle = new Permanent(new DuskLegionDreadnought());
        vehicle.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(vehicle);
        return vehicle;
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
