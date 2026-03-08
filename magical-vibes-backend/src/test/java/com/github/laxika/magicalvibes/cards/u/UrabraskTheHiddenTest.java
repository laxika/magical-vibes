package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UrabraskTheHiddenTest extends BaseCardTest {

    @Test
    @DisplayName("Urabrask has correct static effects")
    void hasCorrectEffects() {
        UrabraskTheHidden card = new UrabraskTheHidden();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);

        GrantKeywordEffect hasteEffect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .findFirst().orElseThrow();
        assertThat(hasteEffect.keyword()).isEqualTo(Keyword.HASTE);
        assertThat(hasteEffect.scope()).isEqualTo(GrantScope.OWN_CREATURES);

        EnterPermanentsOfTypesTappedEffect enterTapped = (EnterPermanentsOfTypesTappedEffect) card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof EnterPermanentsOfTypesTappedEffect)
                .findFirst().orElseThrow();
        assertThat(enterTapped.cardTypes()).containsExactly(CardType.CREATURE);
        assertThat(enterTapped.opponentsOnly()).isTrue();
    }

    @Test
    @DisplayName("Controller's creatures have haste")
    void controllersCreaturesHaveHaste() {
        harness.addToBattlefield(player1, new UrabraskTheHidden());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures do NOT have haste")
    void opponentsCreaturesDoNotHaveHaste() {
        harness.addToBattlefield(player1, new UrabraskTheHidden());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creatures enter tapped")
    void opponentsCreaturesEnterTapped() {
        harness.addToBattlefield(player1, new UrabraskTheHidden());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's creatures do NOT enter tapped")
    void controllersCreaturesDoNotEnterTapped() {
        harness.addToBattlefield(player1, new UrabraskTheHidden());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.isTapped()).isFalse();
    }
}
