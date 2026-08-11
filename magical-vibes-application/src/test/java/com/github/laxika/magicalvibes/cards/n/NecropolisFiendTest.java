package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecropolisFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic creature cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new NecropolisFiend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4, 5, 6));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof NecropolisFiend);
    }

    @Test
    @DisplayName("Exiling X graveyard cards gives the target creature -X/-X")
    void activatedAbilityScalesWithExiledCards() {
        Permanent fiend = addCreatureReady(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Shock()));
        forceMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, target.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(fiend.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The X graveyard cost is checked before paying mana")
    void requiresEnoughCardsForX() {
        Permanent fiend = addCreatureReady(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        List<Card> graveyard = List.of(new Shock());
        harness.setGraveyard(player1, graveyard);
        forceMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(fiend.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new NecropolisFiend());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forceMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The temporary debuff wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        addCreatureReady(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock()));
        forceMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
