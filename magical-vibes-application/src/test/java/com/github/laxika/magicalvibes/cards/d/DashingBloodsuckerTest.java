package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DashingBloodsucker.class, DazzlingTheaterPropRoom.class, GloriousAnthem.class})
class DashingBloodsuckerTest extends BaseCardTest {

    @Test
    void getsPowerBoostAndLifelinkWhenAnEnchantmentYouControlEnters() {
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        harness.setHand(player1, List.of(simpleEnchantment()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void getsPowerBoostAndLifelinkWhenYouFullyUnlockARoom() {
        Permanent room = castRoom();
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void doesNotTriggerForAnOpponentsEnchantment() {
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostAndLifelinkWearOffAtEndOfTurn() {
        Permanent bloodsucker = harness.addToBattlefieldAndReturn(player1, new DashingBloodsucker());
        harness.setHand(player1, List.of(simpleEnchantment()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bloodsucker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bloodsucker)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bloodsucker, Keyword.LIFELINK)).isFalse();
    }

    private Permanent castRoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private Card simpleEnchantment() {
        Card card = new Card();
        card.setName("Test Enchantment");
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("{1}");
        return card;
    }
}
