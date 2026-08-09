package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KodamasMight;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BakuAltarTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the trigger adds a ki counter for an Arcane spell")
    void arcaneSpellAddsKiCounterWhenAccepted() {
        Permanent altar = addAltar(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KodamasMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(altar.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the trigger does not add a ki counter")
    void decliningTriggerDoesNotAddKiCounter() {
        Permanent altar = addAltar(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KodamasMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(altar.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger the altar")
    void nonSpiritNonArcaneSpellDoesNotTrigger() {
        Permanent altar = addAltar(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(altar.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Paying two mana and removing a ki counter creates a Spirit token")
    void activationCreatesSpiritToken() {
        Permanent altar = addAltar(player1);
        altar.setCounterCount(CounterType.KI, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(altar.isTapped()).isTrue();
        assertThat(altar.getCounterCount(CounterType.KI)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Spirit")
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SPIRIT));
    }

    @Test
    @DisplayName("The activation cannot be paid without a ki counter")
    void cannotActivateWithoutKiCounter() {
        addAltar(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAltar(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BakuAltar());
    }
}
