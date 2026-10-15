package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperSuit.class, GrizzlyBears.class, Unsummon.class})
class SuperSuitTest extends BaseCardTest {

    @Test
    @DisplayName("Super Suit enters attached to and untaps the target creature")
    void entersAttachedAndUntapsTargetCreature() {
        Permanent creature = addCreatureReady(player1);
        creature.tap();
        harness.setHand(player1, List.of(new SuperSuit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        Permanent suit = findPermanent(player1, "Super Suit");
        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip attaches Super Suit to a creature you control")
    void equipAttachesToCreature() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new SuperSuit());
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Super Suit cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent creature = addCreatureReady(player2);
        harness.setHand(player1, List.of(new SuperSuit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Super Suit can be cast without a creature you control")
    void canBeCastWithoutControlledCreatures() {
        Permanent opponentCreature = addCreatureReady(player2);
        opponentCreature.tap();

        harness.castFromHand(player1, new SuperSuit(), "{1}{U}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Super Suit").getAttachedTo()).isNull();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Equipping another creature moves the bonus and does not untap it")
    void reequippingMovesBonusWithoutUntapping() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new SuperSuit());
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        secondCreature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, firstCreature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Flash permits casting Super Suit during combat")
    void canBeCastDuringCombat() {
        Permanent creature = addCreatureReady(player1);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        creature.tap();
        harness.setHand(player1, List.of(new SuperSuit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Super Suit").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flash does not permit equipping during combat")
    void cannotEquipDuringCombat() {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new SuperSuit());
        Permanent creature = addCreatureReady(player1);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(suit.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Super Suit remains unattached when its enter trigger's target leaves")
    void targetLeavingInResponseLeavesSuitUnattached() {
        Permanent creature = addCreatureReady(player1);
        creature.tap();
        harness.setHand(player1, List.of(new SuperSuit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Super Suit").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
