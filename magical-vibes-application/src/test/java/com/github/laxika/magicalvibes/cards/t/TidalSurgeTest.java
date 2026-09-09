package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AlabornTrooper;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidalSurge.class, AlabornTrooper.class, AirElemental.class})
class TidalSurgeTest extends BaseCardTest {

    private void castTidalSurge(List<UUID> targets) {
        prepareTidalSurge();
        harness.castAndResolveSorcery(player1, 0, targets);
    }

    private void prepareTidalSurge() {
        harness.setHand(player1, List.of(new TidalSurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Taps up to three target creatures without flying")
    void tapsThreeCreatures() {
        addCreatureReady(player2, new AlabornTrooper());
        addCreatureReady(player2, new AlabornTrooper());
        addCreatureReady(player2, new AlabornTrooper());
        List<UUID> targetIds = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId).toList();

        castTidalSurge(targetIds);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Can target only one creature")
    void tapsOneCreature() {
        addCreatureReady(player2, new AlabornTrooper());
        UUID targetId = harness.getPermanentId(player2, "Alaborn Trooper");

        castTidalSurge(List.of(targetId));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(targetId))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyer() {
        addCreatureReady(player2, new AirElemental());
        UUID flyerId = harness.getPermanentId(player2, "Air Elemental");
        prepareTidalSurge();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(flyerId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void castWithNoTargets() {
        addCreatureReady(player2, new AlabornTrooper());
        UUID creatureId = harness.getPermanentId(player2, "Alaborn Trooper");

        castTidalSurge(List.of());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(creatureId))
                .noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Can tap non-flying creatures controlled by either player without tapping flyers")
    void tapsValidTargetsRegardlessOfController() {
        Permanent ownCreature = addCreatureReady(player1, new AlabornTrooper());
        Permanent opposingCreature = addCreatureReady(player2, new AlabornTrooper());
        Permanent opposingFlyer = addCreatureReady(player2, new AirElemental());

        castTidalSurge(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
        assertThat(opposingFlyer.isTapped()).isFalse();
    }
}
