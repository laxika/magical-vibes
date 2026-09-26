package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RampagingWarMammoth.class, IronStar.class, Millstone.class, Forest.class})
class RampagingWarMammothTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling destroys up to X target artifacts and draws a card")
    void cyclingDestroysArtifactsAndDraws() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new IronStar());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(new RampagingWarMammoth()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.getGameService().activateHandAbility(
                gd, player1, 0, 0, null, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Rampaging War Mammoth");
    }

    @Test
    @DisplayName("Cycling can choose fewer than X artifacts")
    void cyclingCanChooseFewerTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IronStar());
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(new RampagingWarMammoth()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.getGameService().activateHandAbility(
                gd, player1, 0, 0, null, 2, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Cycling rejects non-artifacts and more than X targets")
    void cyclingRejectsIllegalTargets() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IronStar());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RampagingWarMammoth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.getGameService().activateHandAbility(
                gd, player1, 0, 0, null, 1, List.of(artifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.getGameService().activateHandAbility(
                gd, player1, 0, 0, null, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Rampaging War Mammoth");
    }
}
