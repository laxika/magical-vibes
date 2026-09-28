package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeanderedTowershell.class, GrizzlyBears.class})
class MeanderedTowershellTest extends BaseCardTest {

    @Test
    @DisplayName("Meandered Towershell grants islandwalk to the enchanted creature")
    void grantsIslandwalk() {
        Permanent bears = addCreatureReady();
        castAuraOn(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("Attacking exiles the enchanted creature and Meandered Towershell")
    void attackingExilesBothCards() {
        Permanent bears = addCreatureReady();
        Permanent aura = castAuraOn(bears);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(bears)
                .doesNotContain(aura);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(bears.getCard(), aura.getCard());
    }

    @Test
    @DisplayName("The creature returns tapped and attacking with Meandered Towershell attached on its next turn")
    void returnsCreatureAndAuraOnNextTurn() {
        Permanent bears = addCreatureReady();
        Permanent aura = castAuraOn(bears);
        addCreatureReady();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returnedBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getCard().getId()))
                .findFirst()
                .orElseThrow();
        Permanent returnedAura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(aura.getCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedBears).isNotSameAs(bears);
        assertThat(returnedBears.isTapped()).isTrue();
        assertThat(returnedBears.isAttacking()).isTrue();
        assertThat(returnedBears.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(returnedAura.getAttachedTo()).isEqualTo(returnedBears.getId());
    }

    private Permanent addCreatureReady() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }

    private Permanent castAuraOn(Permanent creature) {
        harness.setHand(player1, List.of(new MeanderedTowershell()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Meandered Towershell");
    }
}
