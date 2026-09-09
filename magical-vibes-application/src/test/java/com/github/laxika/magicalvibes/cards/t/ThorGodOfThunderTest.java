package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThorGodOfThunder.class, Divination.class, Forest.class, GrizzlyBears.class,
        Island.class, LeoninScimitar.class, Shock.class})
class ThorGodOfThunderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB only offers an Equipment, instant, or sorcery from your graveyard")
    void etbFiltersGraveyardTargets() {
        LeoninScimitar equipment = new LeoninScimitar();
        Shock instant = new Shock();
        Divination sorcery = new Divination();
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), equipment, new Island(), instant, sorcery));
        harness.setGraveyard(player2, List.of(new Shock()));
        castThor();

        List<UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).containsExactly(equipment.getId(), instant.getId(), sorcery.getId());
    }

    @Test
    @DisplayName("ETB exiles the chosen card and lets its controller play it")
    void etbExilesChosenCardAndGrantsPlayPermission() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castThor();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
        assertThat(gd.exilePlayPermissions).containsEntry(shock.getId(), player1.getId());
    }

    @Test
    @DisplayName("Casting a noncreature spell deals damage equal to its mana value")
    void noncreatureSpellDealsManaValueDamage() {
        harness.addToBattlefield(player1, new ThorGodOfThunder());
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Thor")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThorGodOfThunder());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("ETB cannot target a creature card")
    void etbCannotTargetCreatureCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ThorGodOfThunder()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private void castThor() {
        harness.setHand(player1, List.of(new ThorGodOfThunder()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
