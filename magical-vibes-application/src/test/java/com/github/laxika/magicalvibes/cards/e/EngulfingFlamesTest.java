package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EngulfingFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage and prevents regeneration of the target creature")
    void dealsDamageAndPreventsRegeneration() {
        Permanent skeletons = new Permanent(new DrudgeSkeletons());
        skeletons.setSummoningSick(false);
        skeletons.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(skeletons);
        harness.setHand(player1, List.of(new EngulfingFlames()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, skeletons.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Flashback exiles Engulfing Flames after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new EngulfingFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Engulfing Flames");
        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Engulfing Flames"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new EngulfingFlames()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Fountain of Youth")))
                .isInstanceOf(IllegalStateException.class);
    }
}
