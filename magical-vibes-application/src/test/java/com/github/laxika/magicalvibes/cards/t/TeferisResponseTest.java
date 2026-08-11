package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FallowEarth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HopeTender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeferisResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an opponent's land-targeting spell, preserves the land, and draws two cards")
    void countersLandTargetingSpellAndDrawsTwo() {
        Permanent forest = addForest(player1);
        FallowEarth fallowEarth = new FallowEarth();
        harness.setHand(player2, List.of(fallowEarth));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.setHand(player1, List.of(new TeferisResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, forest.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, fallowEarth.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player2, "Fallow Earth");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Counters an opponent's land-targeting ability and destroys its permanent source")
    void countersLandTargetingAbilityAndDestroysSource() {
        Permanent forest = addForest(player1);
        Permanent hopeTender = harness.addToBattlefieldAndReturn(player2, new HopeTender());
        hopeTender.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new TeferisResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, forest.getId());
        harness.passPriority(player2);
        harness.castInstant(player1, 0, hopeTender.getCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player2, "Hope Tender");
    }

    @Test
    @DisplayName("Cannot target a land-targeting spell controlled by its own caster")
    void cannotTargetOwnLandTargetingSpell() {
        Permanent forest = addForest(player1);
        FallowEarth fallowEarth = new FallowEarth();
        harness.setHand(player1, List.of(fallowEarth, new TeferisResponse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, forest.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fallowEarth.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addForest(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }
}
