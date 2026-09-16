package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AbandonedOutpost;
import com.github.laxika.magicalvibes.cards.d.DiligentFarmhand;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Refresh.class, DiligentFarmhand.class, AbandonedOutpost.class, Forest.class})
class RefreshTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerates the target creature and draws a card")
    void regeneratesTargetAndDraws() {
        harness.addToBattlefield(player1, new DiligentFarmhand());
        harness.setHand(player1, List.of(new Refresh()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Diligent Farmhand"));

        assertThat(findPermanent(player1, "Diligent Farmhand").getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can regenerate a creature an opponent controls")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new DiligentFarmhand());
        harness.setHand(player1, List.of(new Refresh()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Diligent Farmhand"));

        assertThat(findPermanent(player2, "Diligent Farmhand").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not draw if the target creature leaves before resolution")
    void doesNotResolveIfTargetLeavesBeforeResolution() {
        var target = harness.addToBattlefieldAndReturn(player2, new DiligentFarmhand());
        harness.setHand(player1, List.of(new Refresh()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new AbandonedOutpost());
        harness.setHand(player1, List.of(new Refresh()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Abandoned Outpost")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
