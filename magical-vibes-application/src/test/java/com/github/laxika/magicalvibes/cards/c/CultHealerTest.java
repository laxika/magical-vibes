package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({CultHealer.class, GloriousAnthem.class})
class CultHealerTest extends BaseCardTest {

    @Test
    void gainsLifelinkWhenAnEnchantmentEntersUnderItsControllersControl() {
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void doesNotTriggerForAnOpponentsEnchantment() {
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lifelinkWearsOffAtEndOfTurn() {
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void gainsLifelinkWhenAControlledRoomIsFullyUnlocked() {
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        Permanent room = harness.addToBattlefieldAndReturn(player1, testRoom());

        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyRoomFullyUnlockedTriggers(gd, player1.getId(), room));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, healer, Keyword.LIFELINK)).isTrue();
    }

    private Card testRoom() {
        Card room = new Card();
        room.setName("Test Room");
        room.setType(CardType.ENCHANTMENT);
        room.setSubtypes(List.of(CardSubtype.ROOM));
        return room;
    }
}
