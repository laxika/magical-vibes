package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeathBaronTest extends BaseCardTest {

    private Permanent findByName(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Other Zombies you control get +1/+1 and deathtouch")
    void buffsOtherZombies() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player1, new DeathBaron());

        Permanent zombie = findByName(player1, "Gravecrawler");

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Death Baron does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new DeathBaron());

        Permanent baron = findByName(player1, "Death Baron");

        assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, baron, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not buff Zombies controlled by an opponent")
    void doesNotBuffOpponentZombies() {
        harness.addToBattlefield(player1, new DeathBaron());
        harness.addToBattlefield(player2, new Gravecrawler());

        Permanent opponentZombie = findByName(player2, "Gravecrawler");

        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentZombie, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Zombie, non-Skeleton creatures")
    void doesNotBuffOtherCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DeathBaron());

        Permanent bears = findByName(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Two Death Barons buff each other and stack on other Zombies")
    void twoBaronsStack() {
        harness.addToBattlefield(player1, new DeathBaron());
        harness.addToBattlefield(player1, new DeathBaron());
        harness.addToBattlefield(player1, new Gravecrawler());

        for (Permanent baron : gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Death Baron")).toList()) {
            assertThat(gqs.getEffectivePower(gd, baron)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, baron)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, baron, Keyword.DEATHTOUCH)).isTrue();
        }

        Permanent zombie = findByName(player1, "Gravecrawler");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Bonus is removed when Death Baron leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player1, new DeathBaron());

        Permanent zombie = findByName(player1, "Gravecrawler");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Death Baron"));

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.DEATHTOUCH)).isFalse();
    }
}
