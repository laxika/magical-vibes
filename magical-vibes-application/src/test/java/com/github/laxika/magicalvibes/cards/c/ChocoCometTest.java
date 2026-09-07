package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChocoComet.class, Forest.class})
class ChocoCometTest extends BaseCardTest {

    @Test
    void dealsXDamageAndCreatesBirdToken() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ChocoComet()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(birdToken()).isNotNull();
    }

    @Test
    void birdGetsLandfallBoostUntilCleanup() {
        harness.setHand(player1, List.of(new ChocoComet(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        Permanent bird = birdToken();

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
    }

    private Permanent birdToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
