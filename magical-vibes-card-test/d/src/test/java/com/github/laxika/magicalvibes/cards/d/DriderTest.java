package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Drider.class})
class DriderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates a 2/1 black Spider token with reach and menace")
    void combatDamageCreatesSpiderToken() {
        Permanent drider = addCreatureReady(player1, new Drider());
        drider.setAttacking(true);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Spider");
        assertThat(token.getCard().getName()).isEqualTo("Spider");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIDER);
        assertThat(token.getCard().getKeywords()).containsExactlyInAnyOrder(Keyword.REACH, Keyword.MENACE);
    }

    @Test
    @DisplayName("No Spider token is created when Drider is blocked")
    void blockedCombatDamageCreatesNoSpiderToken() {
        Permanent drider = addCreatureReady(player1, new Drider());
        drider.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Drider());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Each Drider creates one token regardless of the amount of combat damage")
    void eachDriderCreatesOneToken() {
        addCreatureReady(player1, new Drider());
        addCreatureReady(player1, new Drider());

        declareAttackers(List.of(0, 1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spider")).isEqualTo(2);
        assertThat(countPermanents(player2, "Spider")).isZero();
    }

    @Test
    @DisplayName("A Drider controlled by the second player creates its token for that player")
    void secondPlayerCreatesTokenUnderTheirControl() {
        addCreatureReady(player2, new Drider());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Spider")).isEqualTo(1);
        assertThat(countPermanents(player1, "Spider")).isZero();
    }
}
