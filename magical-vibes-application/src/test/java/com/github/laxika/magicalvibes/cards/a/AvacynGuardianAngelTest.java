package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvacynGuardianAngelTest extends BaseCardTest {

    @Test
    void firstAbilityPreventsDamageFromChosenColorToAnotherCreature() {
        Permanent avacyn = addAvacyn();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addManaForFirstAbility();

        harness.activateAbility(player1, battlefieldIndex(avacyn), 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void firstAbilityDoesNotPreventDamageFromOtherColors() {
        Permanent avacyn = addAvacyn();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addManaForFirstAbility();

        harness.activateAbility(player1, battlefieldIndex(avacyn), 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(greenShock()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void secondAbilityPreventsDamageToAPlaneswalker() {
        Permanent avacyn = addAvacyn();
        Permanent chandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        addManaForSecondAbility();

        harness.activateAbility(player1, battlefieldIndex(avacyn), 1, null, chandra.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    private Permanent addAvacyn() {
        Permanent avacyn = new Permanent(new AvacynGuardianAngel());
        avacyn.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(avacyn);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return avacyn;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void addManaForFirstAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private void addManaForSecondAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    private Card greenShock() {
        Card card = new Card();
        card.setName("Green Shock");
        card.setType(CardType.INSTANT);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.addEffect(EffectSlot.SPELL, new com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect(2));
        return card;
    }
}
