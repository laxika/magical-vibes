package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeeraWildMage.class, Pyroclasm.class, Forest.class, GrizzlyBears.class})
class NeeraWildMageTest extends BaseCardTest {

    @Test
    @DisplayName("Accepted trigger puts the spell on the library bottom and offers a nonland card for free")
    void acceptedTriggerPutsSpellOnBottomAndCastsRevealedCard() {
        harness.addToBattlefield(player1, new NeeraWildMage());
        Pyroclasm spell = new Pyroclasm();
        GrizzlyBears revealedCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), revealedCreature));

        harness.castSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Declining Neera leaves the cast spell to resolve normally")
    void decliningTriggerLeavesSpellAlone() {
        harness.addToBattlefield(player1, new NeeraWildMage());
        Pyroclasm spell = new Pyroclasm();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 3);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(forest);

        harness.castSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }
}
