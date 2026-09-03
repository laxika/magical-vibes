package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.o.OrderOfTheWhiteShield;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DanceOfTheDead.class, BalduvianBears.class, Disenchant.class, OrderOfTheWhiteShield.class})
class DanceOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving reanimates the creature tapped under your control with the Aura attached")
    void reanimatesTappedAndAttaches() {
        Permanent bears = reanimateBears();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dance of the Dead")
                        && bears.getId().equals(p.getAttachedTo()));
        harness.assertNotInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bears = reanimateBears();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bears = reanimateBears();
        assertThat(bears.isTapped()).isTrue();

        advanceToUpkeep(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature's controller pays {1}{B} at upkeep to untap it")
    void controllerPaysToUntap() {
        Permanent bears = reanimateBears();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the {1}{B} payment leaves the creature tapped")
    void decliningLeavesTapped() {
        Permanent bears = reanimateBears();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting without enough mana leaves the creature tapped")
    void cannotPayLeavesTapped() {
        Permanent bears = reanimateBears();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("When the Aura leaves the battlefield, the reanimated creature is sacrificed")
    void sacrificesCreatureWhenAuraLeaves() {
        reanimateBears();
        Permanent aura = findPermanent(player1, "Dance of the Dead");
        assertThat(aura).isNotNull();

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        harness.setGraveyard(player1, List.of(new Disenchant()));
        UUID targetId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.setHand(player1, List.of(new DanceOfTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("A creature with protection from black cannot remain enchanted")
    void protectionFromBlackCausesAuraAndCreatureToLeave() {
        harness.setGraveyard(player1, List.of(new OrderOfTheWhiteShield()));
        UUID targetId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.setHand(player1, List.of(new DanceOfTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Order of the White Shield");
        harness.assertInGraveyard(player1, "Order of the White Shield");
        harness.assertInGraveyard(player1, "Dance of the Dead");
    }

    @Test
    @DisplayName("Reanimates a creature from an opponent's graveyard under your control")
    void reanimatesFromOpponentGraveyard() {
        harness.setGraveyard(player2, List.of(new BalduvianBears()));
        UUID targetId = gd.playerGraveyards.get(player2.getId()).getFirst().getId();

        harness.setHand(player1, List.of(new DanceOfTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, targetId);
        assertThat(gd.stack.getFirst().getTargetZone()).isEqualTo(Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Balduvian Bears");
        assertThat(bears).isNotNull();
        assertThat(bears.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
    }

    private Permanent reanimateBears() {
        harness.setGraveyard(player1, List.of(new BalduvianBears()));
        UUID targetId = gd.playerGraveyards.get(player1.getId()).getFirst().getId();

        harness.setHand(player1, List.of(new DanceOfTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Balduvian Bears");
        assertThat(bears).isNotNull();
        return bears;
    }
}
