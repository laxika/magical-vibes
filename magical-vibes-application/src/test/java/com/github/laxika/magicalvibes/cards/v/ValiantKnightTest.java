package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValiantKnightTest extends BaseCardTest {

    private Permanent find(Player owner, String name) {
        return findPermanent(owner, name);
    }

    @Test
    @DisplayName("Other Knights you control get +1/+1")
    void otherKnightGetsAnthem() {
        harness.addToBattlefield(player1, new ValiantKnight());
        harness.addToBattlefield(player1, new BenalishKnight());

        Permanent knight = find(player1, "Benalish Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost itself")
    void doesNotBoostItself() {
        harness.addToBattlefield(player1, new ValiantKnight());

        Permanent self = find(player1, "Valiant Knight");
        assertThat(gqs.getEffectivePower(gd, self)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, self)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost non-Knights or opponent Knights")
    void doesNotBoostOthers() {
        harness.addToBattlefield(player1, new ValiantKnight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BenalishKnight());

        assertThat(gqs.getEffectivePower(gd, find(player1, "Grizzly Bears"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, find(player2, "Benalish Knight"))).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability gives double strike to your Knights, including itself")
    void abilityGrantsDoubleStrike() {
        harness.addToBattlefield(player1, new ValiantKnight());
        harness.addToBattlefield(player1, new BenalishKnight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BenalishKnight());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, find(player1, "Valiant Knight"), Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, find(player1, "Benalish Knight"), Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, find(player1, "Grizzly Bears"), Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, find(player2, "Benalish Knight"), Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
