package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaktosTheUnscarred.class, GrizzlyBears.class, GrayOgre.class, HillGiant.class,
        Fireball.class, Ichthyomorphosis.class, Mountain.class})
class HaktosTheUnscarredTest extends BaseCardTest {

    @Test
    @DisplayName("Haktos must attack each combat when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new HaktosTheUnscarred());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Haktos randomly chooses 2, 3, or 4 and protects from every other mana value")
    void choosesManaValueAndProtectsFromOtherValues() {
        Permanent haktos = castAndResolve();
        int chosenNumber = haktos.getChosenNumber();

        assertThat(chosenNumber).isIn(2, 3, 4);

        Map<Integer, Card> cardsByManaValue = Map.of(
                2, new GrizzlyBears(),
                3, new GrayOgre(),
                4, new HillGiant());
        for (Map.Entry<Integer, Card> entry : cardsByManaValue.entrySet()) {
            Permanent source = new Permanent(entry.getValue());
            assertThat(gqs.hasProtectionFromSource(gd, haktos, source))
                    .as("mana value %s", entry.getKey())
                    .isEqualTo(entry.getKey() != chosenNumber);
        }
    }

    @Test
    @DisplayName("Haktos has no mana-value protection if no number was chosen")
    void hasNoProtectionWithoutChosenNumber() {
        Permanent haktos = addCreatureReady(player1, new HaktosTheUnscarred());

        assertThat(gqs.hasProtectionFromSource(gd, haktos, new Permanent(new GrizzlyBears())))
                .isFalse();
    }

    @Test
    void tappedHaktosIsNotRequiredToAttack() {
        Permanent haktos = addCreatureReady(player1, new HaktosTheUnscarred());
        haktos.tap();

        declareAttackers(List.of());

        assertThat(haktos.isAttacking()).isFalse();
    }

    @Test
    void summoningSickHaktosIsNotRequiredToAttack() {
        Permanent haktos = castAndResolve();

        declareAttackers(List.of());

        assertThat(haktos.isAttacking()).isFalse();
    }

    @Test
    void protectsFromManaValueZero() {
        Permanent haktos = castAndResolve();
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThat(gqs.hasProtectionFromSource(gd, haktos, mountain)).isTrue();
    }

    @Test
    void protectedCreatureCannotBlock() {
        Permanent haktos = castAndResolve();
        haktos.setChosenNumber(3);
        haktos.setSummoningSick(false);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void creatureWithChosenManaValueCanBlockAndDealLethalDamage() {
        Permanent haktos = castAndResolve();
        haktos.setChosenNumber(3);
        haktos.setSummoningSick(false);
        addCreatureReady(player2, new GrayOgre());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Haktos the Unscarred");
        harness.assertInGraveyard(player2, "Gray Ogre");
    }

    @Test
    void losingAllAbilitiesRemovesManaValueProtection() {
        Permanent haktos = castAndResolve();
        haktos.setChosenNumber(3);
        harness.setHand(player1, List.of(new Ichthyomorphosis()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, haktos.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, haktos)).isZero();
        assertThat(gqs.hasProtectionFromSource(gd, haktos, new Permanent(new GrizzlyBears())))
                .isFalse();
    }

    @Test
    void xSpellWithChosenManaValueCanTargetAndDamageHaktos() {
        Permanent haktos = castAndResolve();
        int xValue = haktos.getChosenNumber() - 1;
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);

        harness.castSorcery(player1, 0, xValue, List.of(haktos.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Haktos the Unscarred");
    }

    private Permanent castAndResolve() {
        harness.setHand(player1, List.of(new HaktosTheUnscarred()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        return findPermanent(player1, "Haktos the Unscarred");
    }
}
