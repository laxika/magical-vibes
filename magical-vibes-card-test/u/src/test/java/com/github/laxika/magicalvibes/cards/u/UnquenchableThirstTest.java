package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DesertOfTheMindful;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnquenchableThirst.class, FeralProwler.class, DesertOfTheMindful.class})
class UnquenchableThirstTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps enchanted creature when you control a Desert")
    void etbTapsWithDesertOnBattlefield() {
        harness.addToBattlefield(player1, new DesertOfTheMindful());

        Permanent bears = addCreatureReady(player2, new FeralProwler());

        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bears.getId());

        harness.passBothPriorities(); // resolve the Aura — ETB trigger onto stack
        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB taps enchanted creature when a Desert card is in your graveyard")
    void etbTapsWithDesertInGraveyard() {
        harness.setGraveyard(player1, List.of(new DesertOfTheMindful()));

        Permanent bears = addCreatureReady(player2, new FeralProwler());

        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not tap without any Desert, but Aura still attaches")
    void etbNoTapWithoutDesert() {
        Permanent bears = addCreatureReady(player2, new FeralProwler());

        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bears.getId());

        harness.passBothPriorities(); // resolve the Aura — intervening-if fails, no trigger queued

        assertThat(gd.stack).isEmpty();
        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Unquenchable Thirst") && p.isAttached());
    }

    @Test
    @DisplayName("Tapped enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new FeralProwler());
        creature.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnquenchableThirst());
        aura.setAttachedTo(creature.getId());

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps again after Unquenchable Thirst is removed")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new FeralProwler());
        creature.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnquenchableThirst());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new DesertOfTheMindful());

        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, desert.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void opponentsDesertsDoNotEnableTrigger() {
        harness.addToBattlefield(player2, new DesertOfTheMindful());
        harness.setGraveyard(player2, List.of(new DesertOfTheMindful()));
        Permanent creature = addCreatureReady(player2, new FeralProwler());
        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void triggerDoesNothingIfLastDesertLeavesBeforeResolution() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new DesertOfTheMindful());
        Permanent creature = addCreatureReady(player2, new FeralProwler());
        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(desert);
        harness.setHand(player1, List.of(desert.getCard()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void triggerStillTapsIfDesertMovesFromBattlefieldToGraveyard() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new DesertOfTheMindful());
        Permanent creature = addCreatureReady(player2, new FeralProwler());
        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(desert);
        harness.setGraveyard(player1, List.of(desert.getCard()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void losingGraveyardDesertBeforeResolutionStopsTap() {
        harness.setGraveyard(player1, List.of(new DesertOfTheMindful()));
        Permanent creature = addCreatureReady(player2, new FeralProwler());
        harness.setHand(player1, List.of(new UnquenchableThirst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void untapRestrictionDoesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player2, new FeralProwler());
        Permanent otherCreature = addCreatureReady(player2, new FeralProwler());
        creature.tap();
        otherCreature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new UnquenchableThirst());
        aura.setAttachedTo(creature.getId());

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
    }
}
