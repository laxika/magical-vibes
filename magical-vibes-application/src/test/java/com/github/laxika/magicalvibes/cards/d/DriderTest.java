package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Drider.class, GrizzlyBears.class})
class DriderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates a 2/1 black Spider token with reach and menace")
    void combatDamageCreatesSpiderToken() {
        Permanent drider = addCreatureReady(player1, new Drider());
        drider.setAttacking(true);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
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
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
