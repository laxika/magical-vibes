package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrunauCorpseTrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 2/2 black Zombie token")
    void etbCreatesZombieToken() {
        castAndResolveTrawler();

        List<Permanent> tokens = zombieTokens(player1);
        assertThat(tokens).hasSize(1);

        Permanent zombie = tokens.getFirst();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Ability grants deathtouch to target Zombie")
    void abilityGrantsDeathtouch() {
        Permanent trawler = addReadyTrawler(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, trawler.getId());
        harness.passBothPriorities();

        assertThat(trawler.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Can grant deathtouch to the ETB Zombie token")
    void canTargetZombieToken() {
        castAndResolveTrawler();
        Permanent token = zombieTokens(player1).getFirst();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, token.getId());
        harness.passBothPriorities();

        assertThat(token.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchRemovedAtEndOfTurn() {
        Permanent trawler = addReadyTrawler(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, trawler.getId());
        harness.passBothPriorities();
        assertThat(trawler.hasKeyword(Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(trawler.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Zombie creature")
    void cannotTargetNonZombie() {
        addReadyTrawler(player1);
        Permanent bears = addReadyBears(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Zombie");
    }

    private void castAndResolveTrawler() {
        harness.setHand(player1, List.of(new DrunauCorpseTrawler()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private Permanent addReadyTrawler(Player player) {
        Permanent perm = new Permanent(new DrunauCorpseTrawler());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private List<Permanent> zombieTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .filter(p -> p.getCard().isToken())
                .toList();
    }
}
