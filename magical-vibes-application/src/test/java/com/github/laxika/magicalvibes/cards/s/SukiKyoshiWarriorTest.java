package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SukiKyoshiWarrior.class, GrizzlyBears.class})
class SukiKyoshiWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures you control and toughness stays 4")
    void powerEqualsControlledCreatures() {
        Permanent suki = addSukiReady(player1);

        assertThat(gqs.getEffectivePower(gd, suki)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, suki)).isEqualTo(4);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, suki)).isEqualTo(3);
    }

    @Test
    @DisplayName("Power ignores creatures controlled by an opponent")
    void powerIgnoresOpponentsCreatures() {
        Permanent suki = addSukiReady(player1);

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, suki)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking creates a tapped and attacking Ally token")
    void attackingCreatesTappedAndAttackingAllyToken() {
        addSukiReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Ally").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isTrue();
    }

    private Permanent addSukiReady(Player player) {
        Permanent permanent = new Permanent(new SukiKyoshiWarrior());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
