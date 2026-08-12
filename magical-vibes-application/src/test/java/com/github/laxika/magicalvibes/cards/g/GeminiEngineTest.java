package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a Twin token with the source's current power and toughness")
    void attackCreatesTwinWithCurrentPowerAndToughness() {
        Permanent engine = new Permanent(new GeminiEngine());
        engine.setSummoningSick(false);
        engine.setPowerModifier(2);
        engine.setToughnessModifier(1);
        gd.playerBattlefields.get(player1.getId()).add(engine);
        Permanent secondEngine = new Permanent(new GeminiEngine());
        secondEngine.setSummoningSick(false);
        secondEngine.setPowerModifier(2);
        secondEngine.setToughnessModifier(1);
        gd.playerBattlefields.get(player1.getId()).add(secondEngine);

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        Permanent twin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(twin.getCard().getName()).isEqualTo("Twin");
        assertThat(twin.isAttacking()).isTrue();
        assertThat(twin.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, twin)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, twin)).isEqualTo(5);
    }

    @Test
    @DisplayName("Twin is sacrificed at end of combat")
    void twinIsSacrificedAtEndOfCombat() {
        Permanent engine = new Permanent(new GeminiEngine());
        engine.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(engine);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Twin"))).isTrue();
        assertThat(gameLogContains("Twin")).isTrue();
    }
}
