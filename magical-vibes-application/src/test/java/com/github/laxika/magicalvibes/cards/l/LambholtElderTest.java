package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SilverpeltWerewolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NoSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LambholtElderTest extends BaseCardTest {

    @Test
    @DisplayName("Has correct effects configured")
    void hasCorrectEffects() {
        LambholtElder card = new LambholtElder();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isEqualTo(new DrawCardEffect(1));

        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(NoSpellsCastLastTurnConditionalEffect.class);
        NoSpellsCastLastTurnConditionalEffect frontTransform =
                (NoSpellsCastLastTurnConditionalEffect) card.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst();
        assertThat(frontTransform.wrapped()).isInstanceOf(TransformSelfEffect.class);

        assertThat(card.getBackFaceCard()).isInstanceOf(SilverpeltWerewolf.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("SilverpeltWerewolf");
    }

    @Test
    @DisplayName("Back face has correct effects configured")
    void backFaceHasCorrectEffects() {
        LambholtElder card = new LambholtElder();
        SilverpeltWerewolf backFace = (SilverpeltWerewolf) card.getBackFaceCard();

        assertThat(backFace.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isEqualTo(new DrawCardEffect(2));

        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(backFace.getEffects(EffectSlot.EACH_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(TwoOrMoreSpellsCastLastTurnConditionalEffect.class);
    }

    @Test
    @DisplayName("Lambholt Elder draws a card when dealing combat damage to a player")
    void frontFaceDrawsOneCardOnCombatDamage() {
        Permanent elder = addReadyCreature(new LambholtElder());
        elder.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when blocked and no combat damage is dealt to a player")
    void noDrawWhenBlocked() {
        Permanent elder = addReadyCreature(new LambholtElder());
        elder.setAttacking(true);

        Permanent blocker = addReadyCreature(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lambholt Elder"));
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Transforms to Silverpelt Werewolf when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new LambholtElder());
        Permanent elder = findPermanent(player1, "Lambholt Elder");

        gd.spellsCastLastTurn.clear();

        forceUpkeep(player2);
        harness.passBothPriorities();

        assertThat(elder.isTransformed()).isTrue();
        assertThat(elder.getCard().getName()).isEqualTo("Silverpelt Werewolf");
        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new LambholtElder());
        Permanent elder = findPermanent(player1, "Lambholt Elder");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        forceUpkeep(player2);

        assertThat(elder.isTransformed()).isFalse();
        assertThat(elder.getCard().getName()).isEqualTo("Lambholt Elder");
    }

    @Test
    @DisplayName("Silverpelt Werewolf draws two cards when dealing combat damage to a player")
    void backFaceDrawsTwoCardsOnCombatDamage() {
        Permanent elder = addReadyCreature(new LambholtElder());
        transformToBackFace(elder);
        elder.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Silverpelt Werewolf transforms back when a player cast two or more spells last turn")
    void silverpeltTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new LambholtElder());
        Permanent elder = findPermanent(player1, "Lambholt Elder");
        transformToBackFace(elder);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        forceUpkeep(player1);
        harness.passBothPriorities();

        assertThat(elder.isTransformed()).isFalse();
        assertThat(elder.getCard().getName()).isEqualTo("Lambholt Elder");
        assertThat(gqs.getEffectivePower(gd, elder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Silverpelt Werewolf does not transform back when only one spell was cast last turn")
    void silverpeltDoesNotTransformBackWhenOnlyOneSpellCast() {
        harness.addToBattlefield(player1, new LambholtElder());
        Permanent elder = findPermanent(player1, "Lambholt Elder");
        transformToBackFace(elder);

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        forceUpkeep(player1);

        assertThat(elder.isTransformed()).isTrue();
        assertThat(elder.getCard().getName()).isEqualTo("Silverpelt Werewolf");
    }

    private Permanent addReadyCreature(Card card) {
        return addReadyCreature(player1, card);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void forceUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void transformToBackFace(Permanent elder) {
        gd.spellsCastLastTurn.clear();
        forceUpkeep(player2);
        harness.passBothPriorities();
        elder.untap();
        elder.setSummoningSick(false);
        assertThat(elder.isTransformed()).isTrue();
    }
}
