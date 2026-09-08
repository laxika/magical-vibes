package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatriciansScorn.class, GloriousAnthem.class, GrizzlyBears.class, SuntailHawk.class})
class PatriciansScornTest extends BaseCardTest {

    @Test
    void castsForFreeAfterCastingAnotherWhiteSpell() {
        SuntailHawk whiteSpell = new SuntailHawk();
        PatriciansScorn scorn = new PatriciansScorn();
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(whiteSpell, scorn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castInstantWithAlternateCost(player1, 0, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opponentEnchantment)
                .contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scorn);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void castsNormallyWithoutAnotherWhiteSpell() {
        PatriciansScorn scorn = new PatriciansScorn();
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(scorn));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentEnchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scorn);
    }

    @Test
    void cannotUseFreeAlternateCostWithoutAnotherWhiteSpell() {
        PatriciansScorn scorn = new PatriciansScorn();
        harness.setHand(player1, List.of(scorn));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
