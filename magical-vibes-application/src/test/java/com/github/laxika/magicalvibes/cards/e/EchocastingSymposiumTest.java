package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetCreatureForTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EchocastingSymposiumTest extends BaseCardTest {

    @Test
    @DisplayName("Has player and creature targets with token copy effect")
    void hasCorrectStructure() {
        EchocastingSymposium card = new EchocastingSymposium();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getMinTargets()).isEqualTo(2);
        assertThat(card.getMaxTargets()).isEqualTo(2);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(CreateTokenCopyOfTargetCreatureForTargetPlayerEffect.class);
    }

    @Test
    @DisplayName("Target player creates a token copy of target creature you control")
    void targetPlayerCreatesTokenCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EchocastingSymposium()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(player2.getId(), bearId));
        harness.passBothPriorities();

        List<Permanent> opponentBf = gd.playerBattlefields.get(player2.getId());
        assertThat(opponentBf).hasSize(1);
        assertThat(opponentBf.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(opponentBf.getFirst().getCard().isToken()).isTrue();
    }
}
