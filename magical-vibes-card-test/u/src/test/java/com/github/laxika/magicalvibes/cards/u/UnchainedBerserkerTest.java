package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DaybreakChaplain;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnchainedBerserker.class, DaybreakChaplain.class, Pacifism.class, Shock.class})
class UnchainedBerserkerTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createTargetedInstant(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{W}");
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    @Test
    @DisplayName("Gets +2/+0 while attacking")
    void getsPlusTwoPlusZeroWhileAttacking() {
        Permanent berserker = addCreatureReady(player1, new UnchainedBerserker());

        berserker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get the attacking boost while not attacking")
    void noBoostWhileNotAttacking() {
        Permanent berserker = addCreatureReady(player1, new UnchainedBerserker());

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(1);
    }

    @Test
    @DisplayName("White creature cannot block it")
    void whiteCreatureCannotBlock() {
        Permanent berserker = addCreatureReady(player1, new UnchainedBerserker());
        berserker.setAttacking(true);
        addCreatureReady(player2, createCreature("White Bear", 2, 2, CardColor.WHITE));

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by a white instant")
    void cannotBeTargetedByWhiteInstant() {
        Permanent berserker = addCreatureReady(player2, new UnchainedBerserker());
        addCreatureReady(player2, createCreature("Other Creature", 2, 2, CardColor.RED));

        harness.setHand(player1, List.of(createTargetedInstant("White Bolt", CardColor.WHITE)));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, berserker.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("White combat damage is prevented while blocking and gives no lifelink life")
    void preventsWhiteCombatDamageWhileBlocking() {
        Permanent chaplain = addCreatureReady(player2, new DaybreakChaplain());
        chaplain.setAttacking(true);
        Permanent berserker = addCreatureReady(player1, new UnchainedBerserker());
        berserker.setBlocking(true);
        berserker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(1);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Unchained Berserker");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Controller's white Aura cannot target the Berserker")
    void ownWhiteAuraCannotTarget() {
        Permanent berserker = addCreatureReady(player1, new UnchainedBerserker());
        addCreatureReady(player1, new DaybreakChaplain());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, berserker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Protection from white does not stop red targeted damage")
    void redDamageCanKill() {
        Permanent berserker = addCreatureReady(player2, new UnchainedBerserker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, berserker.getId());

        harness.assertInGraveyard(player2, "Unchained Berserker");
    }

    @Test
    @DisplayName("Attacking boost ends when the creature stops attacking")
    void boostEndsWhenNoLongerAttacking() {
        Permanent berserker = addCreatureReady(player1, new UnchainedBerserker());
        berserker.setAttacking(true);
        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);

        berserker.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(1);
    }
}
