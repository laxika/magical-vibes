package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdelineResplendentCathar.class, GrizzlyBears.class})
class AdelineResplendentCatharTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures you control and toughness stays 4")
    void powerEqualsControlledCreatures() {
        Permanent adeline = addAdelineReady(player1);

        assertThat(gqs.getEffectivePower(gd, adeline)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, adeline)).isEqualTo(4);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, adeline)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, adeline)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking creates a tapped and attacking Human token")
    void attackingCreatesHumanToken() {
        addAdelineReady(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());

        Permanent token = findPermanents(player1, "Human").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
    }

    private Permanent addAdelineReady(Player player) {
        Permanent permanent = new Permanent(new AdelineResplendentCathar());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
