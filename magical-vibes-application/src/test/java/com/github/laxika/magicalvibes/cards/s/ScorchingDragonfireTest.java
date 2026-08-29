package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScorchingDragonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature that would die from the damage")
    void exilesLethallyDamagedCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAt(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A creature that survives the damage remains on the battlefield")
    void survivingCreatureRemains() {
        harness.addToBattlefield(player2, new SerraAngel());

        castAt(harness.getPermanentId(player2, "Serra Angel"));

        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Exiles a planeswalker that would die from the damage")
    void exilesLethallyDamagedPlaneswalker() {
        Permanent planeswalker = addReadyPlaneswalker(player2, 3);

        castAt(planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertNotInGraveyard(player2, "Garruk Wildspeaker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Garruk Wildspeaker"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void rejectsLandTarget() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new ScorchingDragonfire()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Plains")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ScorchingDragonfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent permanent = new Permanent(new GarrukWildspeaker());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
