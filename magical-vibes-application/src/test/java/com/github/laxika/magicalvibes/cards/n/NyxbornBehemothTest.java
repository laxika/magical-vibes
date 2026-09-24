package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NyxbornBehemoth.class, GloriousAnthem.class})
class NyxbornBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature enchantments reduce the generic casting cost by their total mana value")
    void noncreatureEnchantmentsReduceCastingCostByTotalManaValue() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new NyxbornBehemoth());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new NyxbornBehemoth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Sacrificing another enchantment grants indestructible until end of turn")
    void sacrificingAnotherEnchantmentGrantsIndestructibleUntilEndOfTurn() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new NyxbornBehemoth());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(behemoth.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(behemoth.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("The ability cannot sacrifice Nyxborn Behemoth itself")
    void cannotSacrificeItself() {
        harness.addToBattlefield(player1, new NyxbornBehemoth());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
