package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StingingHivemaster.class, WrathOfGod.class})
class StingingHivemasterTest extends BaseCardTest {

    @Test
    @DisplayName("When Stinging Hivemaster dies, it creates a Mite token")
    void deathTriggerCreatesMiteToken() {
        harness.addToBattlefield(player1, new StingingHivemaster());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Mite");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.MITE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TOXIC);
        assertThat(bls.canBlock(gd, token)).isFalse();
    }

    @Test
    @DisplayName("The Mite token gives a poison counter when it deals combat damage")
    void miteDealsToxicCombatDamage() {
        harness.addToBattlefield(player1, new StingingHivemaster());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Mite");
        token.setAttacking(true);
        resolveCombat(player1);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hivemaster gives poison alongside its combat damage without a triggered ability")
    void hivemasterDealsToxicCombatDamage() {
        harness.addToBattlefield(player1, new StingingHivemaster());
        findPermanent(player1, "Stinging Hivemaster").setAttacking(true);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous deaths create one Mite for each Hivemaster's controller")
    void simultaneousDeathsCreateTokensForBothControllers() {
        harness.addToBattlefield(player1, new StingingHivemaster());
        harness.addToBattlefield(player2, new StingingHivemaster());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Mite").getCard().isToken()).isTrue();
        assertThat(findPermanent(player2, "Mite").getCard().isToken()).isTrue();
    }
}
