package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DeviantSkytech.class)
class DeviantSkytechTest extends BaseCardTest {

    @Test
    void enteringCreatesTwoThopters() {
        harness.setHand(player1, List.of(new DeviantSkytech()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThopters(2);
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
