package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EnsnaringBridge;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DauthiTrapper.class, YouthfulKnight.class, EnsnaringBridge.class})
class DauthiTrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants shadow to target creature")
    void grantsShadowToTargetCreature() {
        Permanent trapper = addCreatureReady(player1, new DauthiTrapper());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.SHADOW)).isTrue();
        assertThat(gqs.hasKeyword(gd, trapper, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Ability can target an opponent's creature")
    void grantsShadowToOpponentsCreature() {
        addCreatureReady(player1, new DauthiTrapper());
        Permanent knight = addCreatureReady(player2, new YouthfulKnight());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new DauthiTrapper());
        Permanent bridge = harness.addToBattlefieldAndReturn(player2, new EnsnaringBridge());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bridge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Activating the ability taps the source")
    void activationTapsSource() {
        Permanent trapper = addCreatureReady(player1, new DauthiTrapper());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted shadow wears off at end of turn")
    void shadowWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new DauthiTrapper());
        Permanent knight = addCreatureReady(player1, new YouthfulKnight());

        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.SHADOW)).isFalse();
    }
}
