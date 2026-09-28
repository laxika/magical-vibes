package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManTonyStark.class, Divination.class, GrizzlyBears.class, Shock.class})
class IronManTonyStarkTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +1/+0")
    void boostsOwnAttackingCreatures() {
        harness.addToBattlefield(player1, new IronManTonyStark());
        Permanent attackingBears = addCreatureReady(player1, new GrizzlyBears());
        attackingBears.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, attackingBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attackingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a red spell creates a 2/1 colorless Robot Hero artifact token with flying")
    void redSpellCreatesRobotHeroToken() {
        harness.addToBattlefield(player1, new IronManTonyStark());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Robot");
        assertThat(token.getCard().getColors()).isEmpty();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT, CardSubtype.HERO);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a non-red spell does not create a Robot Hero token")
    void nonRedSpellDoesNotCreateRobotHeroToken() {
        harness.addToBattlefield(player1, new IronManTonyStark());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }
}
