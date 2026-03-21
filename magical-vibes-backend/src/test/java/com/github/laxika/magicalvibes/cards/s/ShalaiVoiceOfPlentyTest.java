package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantControllerHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShalaiVoiceOfPlentyTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three static effects")
    void hasThreeStaticEffects() {
        ShalaiVoiceOfPlenty card = new ShalaiVoiceOfPlenty();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(3);
    }

    @Test
    @DisplayName("First static effect grants controller hexproof")
    void firstStaticEffectGrantsControllerHexproof() {
        ShalaiVoiceOfPlenty card = new ShalaiVoiceOfPlenty();

        assertThat(card.getEffects(EffectSlot.STATIC).get(0))
                .isInstanceOf(GrantControllerHexproofEffect.class);
    }

    @Test
    @DisplayName("Second static effect grants hexproof to own planeswalkers")
    void secondStaticEffectGrantsPlaneswalkerHexproof() {
        ShalaiVoiceOfPlenty card = new ShalaiVoiceOfPlenty();

        GrantKeywordEffect effect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).get(1);
        assertThat(effect.keywords()).containsExactly(Keyword.HEXPROOF);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_PERMANENTS);
        assertThat(effect.filter()).isInstanceOf(PermanentIsPlaneswalkerPredicate.class);
    }

    @Test
    @DisplayName("Third static effect grants hexproof to other creatures you control")
    void thirdStaticEffectGrantsCreatureHexproof() {
        ShalaiVoiceOfPlenty card = new ShalaiVoiceOfPlenty();

        GrantKeywordEffect effect = (GrantKeywordEffect) card.getEffects(EffectSlot.STATIC).get(2);
        assertThat(effect.keywords()).containsExactly(Keyword.HEXPROOF);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
    }

    @Test
    @DisplayName("Has one activated ability that puts +1/+1 counters")
    void hasActivatedAbility() {
        ShalaiVoiceOfPlenty card = new ShalaiVoiceOfPlenty();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{4}{G}{G}");
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst())
                .isInstanceOf(PutPlusOnePlusOneCounterOnEachOwnCreatureEffect.class);
    }

    // ===== Controller hexproof =====

    @Test
    @DisplayName("Controller has hexproof while Shalai is on the battlefield")
    void controllerHasHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Opponent does not have hexproof from Shalai")
    void opponentDoesNotHaveHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());

        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Controller loses hexproof when Shalai leaves the battlefield")
    void controllerLosesHexproofWhenShalaiRemoved() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();

        Permanent shalai = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shalai, Voice of Plenty"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(shalai);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }

    // ===== Other creatures hexproof =====

    @Test
    @DisplayName("Other creatures you control have hexproof")
    void otherCreaturesHaveHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Shalai itself does not have hexproof")
    void shalaiDoesNotHaveHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());

        Permanent shalai = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shalai, Voice of Plenty"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, shalai, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creatures do not get hexproof")
    void opponentCreaturesDoNotGetHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot target hexproof creature with a spell")
    void opponentCannotTargetHexproofCreature() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Planeswalker hexproof =====

    @Test
    @DisplayName("Planeswalkers you control have hexproof")
    void planeswalkerHasHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player1, new AjaniGoldmane());

        Permanent ajani = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ajani Goldmane"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, ajani, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Opponent's planeswalkers do not get hexproof")
    void opponentPlaneswalkerDoesNotGetHexproof() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player2, new AjaniGoldmane());

        Permanent opponentAjani = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ajani Goldmane"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, opponentAjani, Keyword.HEXPROOF)).isFalse();
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activated ability puts +1/+1 counter on each creature you control")
    void activatedAbilityPutsCounters() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        Permanent shalai = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Shalai, Voice of Plenty"))
                .findFirst().orElseThrow();

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(shalai.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability does not affect opponent's creatures")
    void activatedAbilityDoesNotAffectOpponent() {
        harness.addToBattlefield(player1, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent opponentBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(opponentBears.getPlusOnePlusOneCounters()).isEqualTo(0);
    }
}
