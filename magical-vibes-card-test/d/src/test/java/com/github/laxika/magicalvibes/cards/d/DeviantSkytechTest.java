package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeviantSkytech.class})
class DeviantSkytechTest extends BaseCardTest {

    @Test
    void enteringCreatesTwoThopters() {
        harness.castFromHand(player1, new DeviantSkytech(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThopters(2);
    }

    @Test
    void enteringStartsOnlyItsControllersEnginesBeforeTheEnterTriggerResolves() {
        harness.castFromHand(player1, new DeviantSkytech(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(findPermanents(player1, "Thopter")).isEmpty();

        harness.passBothPriorities();
        assertThopters(2);
    }

    @Test
    void enteringDoesNotResetExistingSpeed() {
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.castFromHand(player1, new DeviantSkytech(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
        assertThopters(2);
    }

    @Test
    void sacrificingWithTheEnterTriggerPendingStillCreatesFourThopters() {
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.castFromHand(player1, new DeviantSkytech(), "{2}{U}");
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Deviant Skytech");

        harness.activateAbility(player1, 0, null, null);

        assertThat(source).isNotIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
        assertThat(findPermanents(player1, "Thopter")).isEmpty();

        harness.passBothPriorities();
        assertThopters(2);
        harness.passBothPriorities();
        assertThopters(4);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(4);
        assertThat(findPermanents(player2, "Thopter")).isEmpty();
    }

    @Test
    void maxSpeedAbilitySacrificesAndCreatesTwoThopters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DeviantSkytech());
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source).isNotIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
        assertThopters(2);
    }

    @Test
    void maxSpeedAbilityCannotBeActivatedBelowMaxSpeed() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DeviantSkytech());
        gd.playerSpeeds.put(player1.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");

        assertThat(source).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    private void assertThopters(int count) {
        List<Permanent> thopters = findPermanents(player1, "Thopter");
        assertThat(thopters).hasSize(count);
        assertThat(thopters).allSatisfy(thopter -> {
            assertThat(thopter.getEffectivePower()).isEqualTo(1);
            assertThat(thopter.getEffectiveToughness()).isEqualTo(1);
            assertThat(thopter.getCard().getColor()).isNull();
            assertThat(thopter.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }
}
