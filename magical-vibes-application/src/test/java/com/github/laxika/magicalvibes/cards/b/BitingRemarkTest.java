package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SigiledStarfish;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitingRemark.class, SigiledStarfish.class})
class BitingRemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Scrycast casts Biting Remark for zero mana while scrying")
    void scrycastCastsBitingRemark() {
        BitingRemark bitingRemark = new BitingRemark();
        harness.setLibrary(player1, List.of(bitingRemark));
        addReadyStarfish();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(bitingRemark);

        gs.handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(), 0));

        assertThat(gameData.playerDecks.get(player1.getId())).doesNotContain(bitingRemark);
        assertThat(gameData.stack).anyMatch(entry -> entry.getCard() == bitingRemark
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == bitingRemark);
    }

    @Test
    @DisplayName("Biting Remark may be left on top instead of being cast")
    void mayDeclineScrycast() {
        BitingRemark bitingRemark = new BitingRemark();
        harness.setLibrary(player1, List.of(bitingRemark));
        addReadyStarfish();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bitingRemark);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyStarfish() {
        Permanent starfish = harness.addToBattlefieldAndReturn(player1, new SigiledStarfish());
        starfish.setSummoningSick(false);
        return starfish;
    }
}
