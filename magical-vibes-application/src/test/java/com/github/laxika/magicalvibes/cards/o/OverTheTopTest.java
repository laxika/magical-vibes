package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OverTheTopTest extends BaseCardTest {

    @Test
    @DisplayName("Each player reveals based on their nonland permanent count")
    void eachPlayerRevealsBasedOnTheirNonlandPermanentCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        RodOfRuin player1Permanent = new RodOfRuin();
        Shock player1Spell = new Shock();
        GrizzlyBears player2Permanent = new GrizzlyBears();
        Shock player2Spell = new Shock();
        setLibrary(player1, player1Permanent, player1Spell);
        setLibrary(player2, player2Permanent, player2Spell);

        harness.setHand(player1, List.of(new OverTheTop()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rod of Ruin");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Spell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Lands are not counted when determining how many cards to reveal")
    void landsAreNotCounted() {
        harness.addToBattlefield(player1, new Forest());
        Shock shock = new Shock();
        RodOfRuin rodOfRuin = new RodOfRuin();
        setLibrary(player1, shock, rodOfRuin);
        gd.playerDecks.get(player2.getId()).clear();

        harness.setHand(player1, List.of(new OverTheTop()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, rodOfRuin);
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, Card... cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(List.of(cards));
    }
}
