package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KozileksChanneler;
import com.github.laxika.magicalvibes.cards.m.MakindiSliderunner;
import com.github.laxika.magicalvibes.cards.t.TouchOfTheVoid;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LithomancersFocus.class, GrizzlyBears.class, KozileksChanneler.class, MakindiSliderunner.class, TouchOfTheVoid.class})
class LithomancersFocusTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a creature and prevents damage to it from colorless sources")
    void boostsAndProtectsFromColorlessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFocus(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        castDamage(player2, colorlessDamageSpell(), target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage from colored sources")
    void doesNotPreventColoredDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFocus(target);

        castDamage(player2, coloredDamageSpell(CardColor.RED), target);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("The boost and damage prevention expire at the end of the turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFocus(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        castDamage(player2, colorlessDamageSpell(), target);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new LithomancersFocus()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protects an opponent's creature from repeated devoid spell damage")
    void protectsOpponentCreatureFromRepeatedDevoidDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MakindiSliderunner());
        castFocus(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        castTouchOfTheVoid(target);
        castTouchOfTheVoid(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.isExileInsteadOfDieThisTurn()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Prevents colorless damage only to the targeted creature")
    void doesNotProtectOtherCreatures() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new MakindiSliderunner());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new MakindiSliderunner());
        castFocus(protectedCreature);

        castTouchOfTheVoid(otherCreature);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(protectedCreature).doesNotContain(otherCreature);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(otherCreature.getCard().getId()));
    }

    @Test
    @DisplayName("Prevents colorless combat damage while the boosted blocker still deals damage")
    void preventsColorlessCombatDamage() {
        Permanent blocker = addCreatureReady(player1, new MakindiSliderunner());
        Permanent attacker = addCreatureReady(player2, new KozileksChanneler());
        castFocus(blocker);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Kozilek's Channeler");
        harness.assertLife(player1, 20);
    }

    private void castTouchOfTheVoid(Permanent target) {
        harness.setHand(player1, List.of(new TouchOfTheVoid()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void castFocus(Permanent target) {
        harness.setHand(player1, List.of(new LithomancersFocus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void castDamage(com.github.laxika.magicalvibes.model.Player caster, Card damageSpell,
                             Permanent target) {
        harness.setHand(caster, List.of(damageSpell));
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    private Card colorlessDamageSpell() {
        return damageSpell("Colorless Bolt", null);
    }

    private Card coloredDamageSpell(CardColor color) {
        return damageSpell("Red Bolt", color);
    }

    private Card damageSpell(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));
        return card;
    }
}
