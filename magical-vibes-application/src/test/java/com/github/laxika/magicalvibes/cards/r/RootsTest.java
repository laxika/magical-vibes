package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AbbeyGargoyles;
import com.github.laxika.magicalvibes.cards.a.AysenAbbey;
import com.github.laxika.magicalvibes.cards.f.FolkOfAnHavva;
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

@CardUsed({Roots.class, FolkOfAnHavva.class, AbbeyGargoyles.class, AysenAbbey.class})
class RootsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Roots taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new FolkOfAnHavva());

        harness.setHand(player1, List.of(new Roots()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Roots")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent enchanted = addCreatureReady(player2, new FolkOfAnHavva());
        enchanted.tap();
        Permanent other = addCreatureReady(player2, new FolkOfAnHavva());
        other.tap();

        Permanent roots = harness.addToBattlefieldAndReturn(player1, new Roots());
        roots.setAttachedTo(enchanted.getId());

        advanceToUpkeep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature untaps again once Roots leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new FolkOfAnHavva());
        creature.tap();

        Permanent roots = harness.addToBattlefieldAndReturn(player1, new Roots());
        roots.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(roots);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a creature with flying")
    void cannotEnchantFlyingCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent gargoyles = addCreatureReady(player2, new AbbeyGargoyles());
        harness.setHand(player1, List.of(new Roots()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, gargoyles.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreaturePermanent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AysenAbbey());
        harness.setHand(player1, List.of(new Roots()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature without flying");
    }
}
