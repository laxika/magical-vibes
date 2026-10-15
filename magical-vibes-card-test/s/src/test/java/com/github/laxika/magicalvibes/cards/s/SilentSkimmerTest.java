package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilentSkimmer.class})
class SilentSkimmerTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, the defending player loses 2 life")
    void attackCausesDefendingPlayerToLoseLife() {
        int controllerLifeBefore = gd.getLife(player1.getId());
        int defendingPlayerLifeBefore = gd.getLife(player2.getId());
        addCreatureReady(player1, new SilentSkimmer());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(defendingPlayerLifeBefore - 2);
    }

    @Test
    @DisplayName("The trigger affects the player being attacked")
    void attackFromOpponentAffectsPlayerBeingAttacked() {
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());
        addCreatureReady(player2, new SilentSkimmer());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
    }

    @Test
    @DisplayName("Each attacking Silent Skimmer causes its own life loss")
    void multipleAttackersEachCauseLifeLoss() {
        int defendingLifeBefore = gd.getLife(player2.getId());
        int controllerLifeBefore = gd.getLife(player1.getId());
        addCreatureReady(player1, new SilentSkimmer());
        addCreatureReady(player1, new SilentSkimmer());
        addCreatureReady(player1, new SilentSkimmer());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(defendingLifeBefore - 4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("An attack trigger still resolves after Silent Skimmer leaves the battlefield")
    void attackTriggerResolvesAfterSourceLeavesBattlefield() {
        int defendingLifeBefore = gd.getLife(player2.getId());
        var skimmer = addCreatureReady(player1, new SilentSkimmer());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(defendingLifeBefore);
        gd.playerBattlefields.get(player1.getId()).remove(skimmer);
        gd.playerGraveyards.get(player1.getId()).add(skimmer.getCard());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(defendingLifeBefore - 2);
    }
}
