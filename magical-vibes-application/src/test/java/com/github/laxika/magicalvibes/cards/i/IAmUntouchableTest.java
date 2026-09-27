package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IAmUntouchable.class, GrizzlyBears.class})
class IAmUntouchableTest extends BaseCardTest {

    @Test
    void givesControllerAndTheirPermanentsHexproof() {
        harness.addToBattlefield(player1, new IAmUntouchable());
        Permanent ownPermanent = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingPermanent = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
        assertThat(gqs.hasKeyword(gd, ownPermanent, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingPermanent, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void createsVigilantScarecrowAndAbandonsWhenCombatDamageIsDealt() {
        Permanent scheme = harness.addToBattlefieldAndReturn(player2, new IAmUntouchable());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(scheme);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(scheme.getCard());

        List<Permanent> tokens = findPermanents(player2, "Scarecrow");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SCARECROW);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }
}
