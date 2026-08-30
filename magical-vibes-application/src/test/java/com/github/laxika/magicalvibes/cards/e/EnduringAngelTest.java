package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnduringAngel.class, AngelicEnforcer.class})
class EnduringAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms and sets its controller's life to 3 instead of losing at 0 life")
    void replacesLifeLoss() {
        Permanent angel = addReadyAngel(player1);
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.getLife(player1.getId())).isEqualTo(3);
        assertThat(angel.isTransformed()).isTrue();
        assertThat(angel.getCard()).isInstanceOf(AngelicEnforcer.class);
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Angelic Enforcer's power and toughness equal its controller's life")
    void backFacePowerToughnessEqualsLife() {
        Permanent angel = addTransformedAngel(player1);
        harness.setLife(player1, 7);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(7);
    }

    @Test
    @DisplayName("Angelic Enforcer doubles its controller's life when it attacks")
    void doublesLifeWhenAttacking() {
        Permanent angel = addTransformedAngel(player1);
        harness.setLife(player1, 7);
        declareAttackers(List.of(0));

        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
    }

    private Permanent addReadyAngel(Player player) {
        Permanent angel = new Permanent(new EnduringAngel());
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(angel);
        return angel;
    }

    private Permanent addTransformedAngel(Player player) {
        EnduringAngel card = new EnduringAngel();
        Permanent angel = new Permanent(card);
        angel.setSummoningSick(false);
        angel.setCard(card.getBackFaceCard());
        angel.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(angel);
        return angel;
    }
}
