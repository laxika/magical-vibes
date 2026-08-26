package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.y.YokedOx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BruseTarlRovingRancher.class, GrizzlyBears.class, LlanowarElves.class, Plains.class, YokedOx.class})
class BruseTarlRovingRancherTest extends BaseCardTest {

    @Test
    @DisplayName("Oxen you control have double strike")
    void oxenHaveDoubleStrike() {
        addReady(new BruseTarlRovingRancher());
        Permanent ox = addReady(new YokedOx());
        Permanent nonOx = addReady(new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ox, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonOx, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Entering with a land creates a 2/2 white Ox token")
    void landEntryCreatesOxToken() {
        harness.setHand(player1, List.of(new BruseTarlRovingRancher()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent oxToken = findPermanent(player1, "Ox");
        assertThat(oxToken).isNotNull();
        assertThat(oxToken.getCard().getPower()).isEqualTo(2);
        assertThat(oxToken.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, oxToken, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    @DisplayName("Entering with a nonland grants cast permission until the end of the next turn")
    void nonlandEntryGrantsNextTurnCastPermission() {
        harness.setHand(player1, List.of(new BruseTarlRovingRancher()));
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("Attacking repeats the top-card branch")
    void attackingGrantsCastPermission() {
        Permanent bruse = addReady(new BruseTarlRovingRancher());
        LlanowarElves topCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(battlefieldIndex(bruse)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
