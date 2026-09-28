package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@DisplayName("Bladewing, Deathless Tyrant")
@CardUsed({BladewingDeathlessTyrant.class, GrizzlyBears.class, LightningBolt.class})
class BladewingDeathlessTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates a Zombie Knight for each creature card in its graveyard")
    void combatDamageCreatesTokensForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));
        Permanent bladewing = addCreatureReady(player1, new BladewingDeathlessTyrant());
        bladewing.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Zombie Knight").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE, CardSubtype.KNIGHT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.MENACE);
        });
    }

    @Test
    @DisplayName("Blocked combat damage does not create tokens")
    void blockedCombatDamageDoesNotCreateTokens() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        Permanent bladewing = addCreatureReady(player1, new BladewingDeathlessTyrant());
        bladewing.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(findPermanents(player1, "Zombie Knight").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }
}
