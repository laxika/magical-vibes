package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalFireSage.class})
class LoyalFireSageTest extends BaseCardTest {

    @Test
    void attackingAddsRedManaUntilEndOfCombat() {
        Permanent sage = addCreatureReady(player1, new LoyalFireSage());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sage)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void activatedAbilityCreatesWhiteAllyToken() {
        Permanent sage = addCreatureReady(player1, new LoyalFireSage());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sage), 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Ally").getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ALLY);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    void firebendingManaCanPayForAllyCreationDuringCombat() {
        Permanent sage = addCreatureReady(player1, new LoyalFireSage());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sage)));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sage), 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Ally")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void tappedSummoningSickSageCanActivateRepeatedly() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new LoyalFireSage());
        sage.setSummoningSick(true);
        sage.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sage), 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sage), 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Ally")).isEqualTo(2);
        assertThat(countPermanents(player2, "Ally")).isZero();
        assertThat(sage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void firebendingAddsManaToAttackingController() {
        Permanent sage = addCreatureReady(player2, new LoyalFireSage());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(sage)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
}
