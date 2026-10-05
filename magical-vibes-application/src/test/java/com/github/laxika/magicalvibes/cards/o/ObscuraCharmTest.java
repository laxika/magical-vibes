package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.cards.b.BoonOfSafety;
import com.github.laxika.magicalvibes.cards.b.BrokersAscendancy;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GiftOfOrzhova;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.MaestrosDiabolist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObscuraCharm.class, CivilServant.class, Divination.class, GrizzlyBears.class,
        HillGiant.class, JaceBeleren.class, BoonOfSafety.class, BrokersAscendancy.class,
        GiftOfOrzhova.class, MaestrosDiabolist.class, ObscuraInterceptor.class})
class ObscuraCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target multicolored permanent with mana value 3 or less tapped")
    void returnsMulticoloredPermanentTapped() {
        Card returnedCard = new CivilServant();
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        harness.castInstant(player1, 0, 0, returnedCard.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(returnedCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot return a monocolored card from the graveyard")
    void cannotReturnMonocoloredCard() {
        Card monocoloredCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(monocoloredCard));
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, monocoloredCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters an instant or sorcery spell")
    void countersInstantOrSorcery() {
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new ObscuraCharm()));
        addCharmMana(player2);

        harness.castSorcery(player1, 0, List.of());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, divination.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot counter a creature spell")
    void cannotCounterCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ObscuraCharm()));
        addCharmMana(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a creature or planeswalker with mana value 3 or less")
    void destroysSmallCreatureOrPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        harness.castInstant(player1, 0, 2, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jace Beleren");
    }

    @Test
    @DisplayName("Cannot destroy a creature with mana value greater than 3")
    void cannotDestroyExpensiveCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsNoncreaturePermanentAtManaValueThreeTapped() {
        Card enchantment = new BrokersAscendancy();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        harness.castInstant(player1, 0, 0, enchantment.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brokers Ascendancy");
        harness.assertNotInGraveyard(player1, "Brokers Ascendancy");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void cannotReturnMulticoloredPermanentAboveManaValueThree() {
        Card expensive = new ObscuraInterceptor();
        harness.setGraveyard(player1, List.of(expensive));
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, expensive.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnMulticoloredInstant() {
        Card instant = new ObscuraCharm();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnCardFromOpponentsGraveyard() {
        Card creature = new CivilServant();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersInstantWithoutResolvingItsEffects() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CivilServant());
        BoonOfSafety boon = new BoonOfSafety();
        harness.setHand(player1, List.of(boon));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new ObscuraCharm()));
        addCharmMana(player2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, boon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boon of Safety");
        assertThat(creature.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.SHIELD)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void destroysCreatureAtManaValueThree() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MaestrosDiabolist());
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        harness.castInstant(player1, 0, 2, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Maestros Diabolist");
        harness.assertInGraveyard(player2, "Maestros Diabolist");
    }

    @Test
    void cannotDestroyNoncreatureEnchantmentAtManaValueThree() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new BrokersAscendancy());
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returningAuraChoosesWhatItEnchantsAndEntersTapped() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CivilServant());
        harness.addToBattlefield(player1, new MaestrosDiabolist());
        Card aura = new GiftOfOrzhova();
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        harness.castInstant(player1, 0, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, creature.getId());

        harness.assertNotInGraveyard(player1, "Gift of Orzhova");
        Permanent returnedAura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(aura.getId()))
                .findFirst().orElseThrow();
        assertThat(returnedAura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(returnedAura.isTapped()).isTrue();
    }

    @Test
    void auraRemainsInGraveyardWhenNothingCanBeEnchanted() {
        Card aura = new GiftOfOrzhova();
        harness.setGraveyard(player1, List.of(aura));
        harness.setHand(player1, List.of(new ObscuraCharm()));
        addCharmMana(player1);

        harness.castInstant(player1, 0, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gift of Orzhova");
        harness.assertNotOnBattlefield(player1, "Gift of Orzhova");
    }

    private void addCharmMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
