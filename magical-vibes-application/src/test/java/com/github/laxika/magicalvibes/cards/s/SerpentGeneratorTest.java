package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Delirium;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerpentGenerator.class})
class SerpentGeneratorTest extends BaseCardTest {

    private Permanent addReadyGenerator() {
        return addCreatureReady(player1, new SerpentGenerator());
    }

    private Permanent createSnakeToken() {
        addReadyGenerator();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        return findPermanent(player1, "Snake");
    }

    @Test
    @DisplayName("Activating the ability creates a 1/1 colorless Snake artifact creature token")
    void createsSnakeToken() {
        Permanent token = createSnakeToken();

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SNAKE);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("The created Snake token gives a poison counter when it deals combat damage to a player")
    void snakeTokenGivesPoisonOnCombatDamage() {
        Permanent token = createSnakeToken();
        token.setSummoningSick(false);
        token.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @CardUsed({SerpentGenerator.class, Delirium.class})
    @DisplayName("The created Snake token gives a poison counter when it deals noncombat damage to a player")
    void snakeTokenGivesPoisonOnNoncombatDamage() {
        harness.forceActivePlayer(player2);
        addCreatureReady(player2, new SerpentGenerator());
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player2, "Snake");
        harness.setHand(player1, List.of(new Delirium()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
