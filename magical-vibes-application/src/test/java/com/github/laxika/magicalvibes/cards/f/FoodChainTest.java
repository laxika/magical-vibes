package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FoodChainTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature adds one plus its mana value in creature-spell-only mana")
    void exilingCreatureAddsRestrictedMana() {
        harness.addToBattlefield(player1, new FoodChain());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("The controller chooses which creature to exile when several are available")
    void choosesCreatureToExile() {
        harness.addToBattlefield(player1, new FoodChain());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, second.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(second.getCard().getId());
    }

    @Test
    @DisplayName("Creature-spell-only mana can cast a creature spell")
    void restrictedManaCastsCreatureSpell() {
        harness.addToBattlefield(player1, new FoodChain());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Creature-spell-only mana cannot cast a noncreature spell")
    void restrictedManaCannotCastNoncreatureSpell() {
        harness.addToBattlefield(player1, new FoodChain());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        Card sorcery = new Card();
        sorcery.setName("Test Sorcery");
        sorcery.setType(CardType.SORCERY);
        sorcery.setManaCost("{G}");
        sorcery.setColor(CardColor.GREEN);
        harness.setHand(player1, List.of(sorcery));

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
