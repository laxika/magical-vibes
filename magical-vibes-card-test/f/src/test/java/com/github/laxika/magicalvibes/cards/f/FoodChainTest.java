package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HoodedHydra;
import com.github.laxika.magicalvibes.cards.n.NaturalAffinity;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.Squallmonger;
import com.github.laxika.magicalvibes.cards.r.RushwoodDryad;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoodChain.class, Forest.class, NaturalAffinity.class, RushwoodDryad.class,
        HoodedHydra.class, Opalescence.class, Squallmonger.class})
class FoodChainTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature adds one plus its mana value in creature-spell-only mana")
    void exilingCreatureAddsRestrictedMana() {
        harness.addToBattlefield(player1, new FoodChain());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RushwoodDryad());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Rushwood Dryad");
    }

    @Test
    @DisplayName("The controller chooses which creature to exile when several are available")
    void choosesCreatureToExile() {
        harness.addToBattlefield(player1, new FoodChain());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RushwoodDryad());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RushwoodDryad());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(second.getCard().getId());
    }

    @Test
    @DisplayName("The exile cost only accepts a creature controlled by the activator")
    void exileCostRequiresControlledCreature() {
        harness.addToBattlefield(player1, new FoodChain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new RushwoodDryad());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature-spell-only mana can cast a creature spell")
    void restrictedManaCastsCreatureSpell() {
        harness.addToBattlefield(player1, new FoodChain());
        harness.addToBattlefield(player1, new RushwoodDryad());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.setHand(player1, List.of(new RushwoodDryad()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Rushwood Dryad");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.GREEN))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Creature-spell-only mana cannot cast a noncreature spell")
    void restrictedManaCannotCastNoncreatureSpell() {
        harness.addToBattlefield(player1, new FoodChain());
        harness.addToBattlefield(player1, new RushwoodDryad());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.setHand(player1, List.of(new NaturalAffinity()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An animated Food Chain may exile itself to produce four mana")
    void animatedFoodChainCanExileItself() {
        Permanent chain = harness.addToBattlefieldAndReturn(player1, new FoodChain());
        harness.addToBattlefield(player1, new Opalescence());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(chain);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(chain.getCard().getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.WHITE))
                .isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling an animated land produces one mana")
    void animatedLandProducesOneMana() {
        harness.addToBattlefield(player1, new FoodChain());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new NaturalAffinity(), "{2}{G}");
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.BLACK))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Exiling a face-down creature produces one mana rather than its printed mana value plus one")
    void faceDownCreatureProducesOneMana() {
        harness.addToBattlefield(player1, new FoodChain());
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a face-down creature among several uses its face-down mana value")
    void chosenFaceDownCreatureProducesOneMana() {
        harness.addToBattlefield(player1, new FoodChain());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new RushwoodDryad());
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent faceDown = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown).findFirst().orElseThrow();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, faceDown.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other).doesNotContain(faceDown);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Creature-spell-only mana cannot pay for a creature's activated ability")
    void restrictedManaCannotActivateCreatureAbility() {
        harness.addToBattlefield(player1, new FoodChain());
        harness.addToBattlefield(player1, new RushwoodDryad());
        harness.addToBattlefield(player1, new Squallmonger());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Rushwood Dryad"));
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.GREEN))
                .isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mana ability resolves immediately and can be activated repeatedly")
    void repeatedActivationsResolveWithoutUsingStack() {
        harness.addToBattlefield(player1, new FoodChain());
        harness.addToBattlefield(player1, new RushwoodDryad());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RushwoodDryad());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.stack).isEmpty();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(3);
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.exiledCards).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
