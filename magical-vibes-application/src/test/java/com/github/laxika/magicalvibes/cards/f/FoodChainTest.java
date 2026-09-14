package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.n.NaturalAffinity;
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

@CardUsed({FoodChain.class, Forest.class, NaturalAffinity.class, RushwoodDryad.class})
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
}
