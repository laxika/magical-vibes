package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HerosDownfall;
import com.github.laxika.magicalvibes.cards.r.ReturnedPhalanx;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ErebosGodOfTheDead.class, Forest.class, ReturnedPhalanx.class, MycosynthLattice.class, HerosDownfall.class})
class ErebosGodOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Erebos is not a creature below five devotion to black")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent erebos = addErebos();
        addBlackPermanents(3);

        assertThat(gqs.isCreature(gd, erebos)).isFalse();
        assertThat(gqs.isEnchantment(gd, erebos)).isTrue();
    }

    @Test
    @DisplayName("Erebos becomes a creature at five devotion to black")
    void becomesCreatureAtDevotionThreshold() {
        Permanent erebos = addErebos();
        addBlackPermanents(4);

        assertThat(gqs.isCreature(gd, erebos)).isTrue();
    }

    @Test
    @DisplayName("Erebos prevents opponents from gaining life but not its controller")
    void preventsOpponentsFromGainingLife() {
        addErebos();

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Paying {1}{B} and 2 life draws a card")
    void payingManaAndLifeDrawsACard() {
        harness.addToBattlefield(player1, new ErebosGodOfTheDead());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void stopsBeingCreatureWhenDevotionFalls() {
        Permanent erebos = addErebos();
        addBlackPermanents(4);
        assertThat(gqs.isCreature(gd, erebos)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeLast();

        assertThat(gqs.isCreature(gd, erebos)).isFalse();
        assertThat(gqs.isEnchantment(gd, erebos)).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    void opponentsPermanentsDoNotCountTowardDevotion() {
        Permanent erebos = addErebos();
        addBlackPermanents(3);
        harness.addToBattlefield(player2, new ReturnedPhalanx());

        assertThat(gqs.isCreature(gd, erebos)).isFalse();
    }

    @Test
    void lifeGainRestrictionEndsWhenErebosLeaves() {
        Permanent erebos = addErebos();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(erebos);

        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isTrue();
    }

    @Test
    void cannotActivateWithInsufficientLife() {
        addErebos();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void lifeIsPaidBeforeDrawResolves() {
        addErebos();
        harness.setHand(player1, List.of());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void drawAbilityResolvesAfterErebosLeaves() {
        Permanent erebos = addErebos();
        harness.setHand(player1, List.of());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(erebos);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void losingCreatureTypePreservesArtifactTypeFromEarlierLattice() {
        harness.enterBattlefieldAndReturn(player1, new MycosynthLattice());
        Permanent erebos = harness.enterBattlefieldAndReturn(player1, new ErebosGodOfTheDead());

        assertThat(gqs.isCreature(gd, erebos)).isFalse();
        assertThat(gqs.isEnchantment(gd, erebos)).isTrue();
        assertThat(gqs.isArtifact(gd, erebos)).isTrue();
    }

    @Test
    void indestructiblePreventsDestructionWhileCreature() {
        Permanent erebos = addErebos();
        addBlackPermanents(4);
        harness.setHand(player1, List.of(new HerosDownfall()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, erebos.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(erebos);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof ErebosGodOfTheDead);
    }

    @Test
    void genericManaCannotPayBlackActivationCost() {
        addErebos();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addErebos() {
        return harness.addToBattlefieldAndReturn(player1, new ErebosGodOfTheDead());
    }

    private void addBlackPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new ReturnedPhalanx());
        }
    }

}
