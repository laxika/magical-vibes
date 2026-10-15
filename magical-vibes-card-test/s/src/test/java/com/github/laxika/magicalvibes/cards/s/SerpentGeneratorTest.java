package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Delirium;
import com.github.laxika.magicalvibes.cards.p.PitScorpion;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentGenerator.class, Delirium.class, PitScorpion.class})
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
        Permanent generator = findPermanent(player1, "Serpent Generator");

        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getColors()).isEmpty();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SNAKE);
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(generator.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The created Snake token gives a poison counter when it deals combat damage to a player")
    void snakeTokenGivesPoisonOnCombatDamage() {
        Permanent token = createSnakeToken();
        token.setSummoningSick(false);
        token.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

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

        harness.castAndResolveInstant(player1, 0, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @CardUsed({SerpentGenerator.class, PitScorpion.class})
    @DisplayName("The created Snake token does not give poison when it deals damage to a creature")
    void snakeTokenDoesNotGivePoisonWhenBlocked() {
        Permanent token = createSnakeToken();
        token.setSummoningSick(false);
        token.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PitScorpion());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(token));

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A newly controlled noncreature Generator can activate immediately")
    void newlyControlledGeneratorCanActivate() {
        harness.addToBattlefield(player1, new SerpentGenerator());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
        assertThat(findPermanent(player1, "Snake").isSummoningSick()).isTrue();
        assertThat(findPermanent(player1, "Snake").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activation requires all four mana")
    void cannotActivateWithInsufficientMana() {
        Permanent generator = addReadyGenerator();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(generator.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Snake")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("A tapped Generator cannot activate again even with enough mana")
    void cannotActivateTappedGenerator() {
        createSnakeToken();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("The activated ability creates its token even after the Generator leaves")
    void abilityResolvesWithoutGenerator() {
        Permanent generator = addReadyGenerator();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(generator);
        gd.playerGraveyards.get(player1.getId()).add(generator.getCard());

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
        assertThat(countPermanents(player1, "Serpent Generator")).isZero();
    }

}
