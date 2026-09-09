package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
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

@CardUsed({PriceOfLoyalty.class, GrizzlyBears.class, WilyGoblin.class, Pacifism.class})
class PriceOfLoyaltyTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, steals, and grants haste to the target creature")
    void untapsStealsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castPriceOfLoyalty(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Treasure mana gives the stolen creature +2/+0")
    void treasureManaBoostsTarget() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        harness.setHand(player1, List.of(new WilyGoblin(), new PriceOfLoyalty()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Temporary control, haste, and Treasure boost expire at end of turn")
    void temporaryEffectsExpire() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castPriceOfLoyaltyWithTreasure(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Price of Loyalty only targets creatures")
    void rejectsNonCreaturePermanent() {
        addCreatureReady(player2, new GrizzlyBears());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.setHand(player1, List.of(new PriceOfLoyalty()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castPriceOfLoyalty(Permanent target) {
        harness.setHand(player1, List.of(new PriceOfLoyalty()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void castPriceOfLoyaltyWithTreasure(Permanent target) {
        harness.setHand(player1, List.of(new WilyGoblin(), new PriceOfLoyalty()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Treasure"));
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, "RED");

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
