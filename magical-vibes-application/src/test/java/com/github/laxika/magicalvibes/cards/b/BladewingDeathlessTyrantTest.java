package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.n.NightsWhisper;
import com.github.laxika.magicalvibes.cards.j.JaredCarthalion;
import com.github.laxika.magicalvibes.cards.z.ZetalpaPrimalDawn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Bladewing, Deathless Tyrant")
@CardUsed({BladewingDeathlessTyrant.class, NightsWhisper.class,
        JaredCarthalion.class, ZetalpaPrimalDawn.class})
class BladewingDeathlessTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates a Zombie Knight for each creature card in its graveyard")
    void combatDamageCreatesTokensForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new ZetalpaPrimalDawn(), new ZetalpaPrimalDawn(), new NightsWhisper()));
        Permanent bladewing = addCreatureReady(player1, new BladewingDeathlessTyrant());
        bladewing.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Zombie Knight");
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
        harness.setGraveyard(player1, List.of(new ZetalpaPrimalDawn(), new ZetalpaPrimalDawn()));
        Permanent bladewing = addCreatureReady(player1, new BladewingDeathlessTyrant());
        bladewing.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZetalpaPrimalDawn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Zombie Knight")).isZero();
    }

    @Test
    @DisplayName("Combat damage to a planeswalker creates tokens even when that planeswalker dies")
    void combatDamageToPlaneswalkerCreatesTokens() {
        harness.setGraveyard(player1, List.of(new BladewingDeathlessTyrant()));
        Permanent planeswalker = new Permanent(new JaredCarthalion());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        Permanent bladewing = addCreatureReady(player1, new BladewingDeathlessTyrant());
        bladewing.setAttacking(true);
        bladewing.setAttackTarget(planeswalker.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
        assertThat(countPermanents(player1, "Zombie Knight")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature cards are counted at resolution and only in the controller's graveyard")
    void countsControllerGraveyardAtResolution() {
        harness.setGraveyard(player2, List.of(new BladewingDeathlessTyrant()));
        Permanent bladewing = addCreatureReady(player1, new BladewingDeathlessTyrant());
        bladewing.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of(new BladewingDeathlessTyrant(), new BladewingDeathlessTyrant()));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Zombie Knight")).isEqualTo(2);
        assertThat(countPermanents(player2, "Zombie Knight")).isZero();
    }

    @Test
    @DisplayName("An empty controller graveyard creates no tokens despite opposing creature cards")
    void emptyControllerGraveyardCreatesNoTokens() {
        harness.setGraveyard(player2, List.of(new BladewingDeathlessTyrant()));
        Permanent bladewing = addCreatureReady(player1, new BladewingDeathlessTyrant());
        bladewing.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Zombie Knight")).isZero();
    }
}
