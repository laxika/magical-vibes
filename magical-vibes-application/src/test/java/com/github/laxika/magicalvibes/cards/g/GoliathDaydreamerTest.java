package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoliathDaydreamerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a hand-cast instant with a dream counter after it resolves")
    void exilesResolvingHandSpellWithDreamCounter() {
        harness.addToBattlefield(player1, new GoliathDaydreamer());
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(ritual));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
        assertThat(gd.exiledCardDreamCounters).containsEntry(ritual.getId(), 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(ritual.getId()));
    }

    @Test
    @DisplayName("Attacking offers one owned dream-counter spell to cast for free")
    void attackingOffersDreamCounterSpell() {
        addCreatureReady(player1, new GoliathDaydreamer());
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player1, List.of(ritual));
        gd.exiledCardDreamCounters.put(ritual.getId(), 1);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(ritual.getId())).isNull();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(ritual.getId()));
    }

    @Test
    @DisplayName("Attacking does not offer an exiled spell without a dream counter")
    void attackingIgnoresExiledSpellWithoutDreamCounter() {
        addCreatureReady(player1, new GoliathDaydreamer());
        harness.setExile(player1, List.of(new DarkRitual()));

        declareAttackers(player1, List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
