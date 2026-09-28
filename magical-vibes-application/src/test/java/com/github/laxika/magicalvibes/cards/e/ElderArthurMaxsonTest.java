package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZurgoThundersDecree;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElderArthurMaxson.class, GrizzlyBears.class, ZurgoThundersDecree.class})
class ElderArthurMaxsonTest extends BaseCardTest {

    @Test
    @DisplayName("Elder Arthur Maxson gives training to creature tokens you control")
    void givesTrainingToCreatureTokens() {
        addReadyArthur();
        Permanent zurgo = addCreatureReady(player1, new ZurgoThundersDecree());

        declareAttackers(java.util.List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(zurgo)));
        harness.passBothPriorities();

        Permanent warrior = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.TRAINING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing another creature grants indestructible until end of turn")
    void sacrificesAnotherCreatureAndGainsIndestructible() {
        Permanent arthur = addReadyArthur();
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, arthur, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, arthur, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without another creature")
    void cannotActivateWithoutAnotherCreature() {
        addReadyArthur();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArthur() {
        return addCreatureReady(player1, new ElderArthurMaxson());
    }
}
