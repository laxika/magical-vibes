package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AeonEngine.class)
class AeonEngineTest extends BaseCardTest {

    @Test
    void entersTappedAndReversesTurnOrderWhenActivated() {
        AeonEngine card = new AeonEngine();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent engine = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(engine.isTapped()).isTrue();

        List<UUID> originalOrder = List.copyOf(gd.orderedPlayerIds);
        engine.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.orderedPlayerIds).containsExactly(originalOrder.get(1), originalOrder.get(0));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }
}
