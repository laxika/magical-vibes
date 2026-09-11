package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DrownerOfHope;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FromBeyond.class, DrownerOfHope.class, GrizzlyBears.class})
class FromBeyondTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Eldrazi Scion at the beginning of your upkeep")
    void createsEldraziScionOnUpkeep() {
        harness.addToBattlefield(player1, new FromBeyond());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("The Eldrazi Scion can be sacrificed for colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        harness.addToBattlefield(player1, new FromBeyond());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing From Beyond searches for an Eldrazi card")
    void sacrificesToSearchForEldrazi() {
        harness.addToBattlefield(player1, new FromBeyond());
        DrownerOfHope eldrazi = new DrownerOfHope();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), eldrazi));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(eldrazi);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Drowner of Hope");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
