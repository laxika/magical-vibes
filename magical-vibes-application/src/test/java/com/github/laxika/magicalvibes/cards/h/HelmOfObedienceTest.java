package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HelmOfObedienceTest extends BaseCardTest {

    @Test
    @DisplayName("Stops at the first milled creature, sacrifices itself and reanimates it under your control")
    void millsUntilCreatureAndReanimates() {
        harness.addToBattlefield(player1, new HelmOfObedience());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        Card bears = new GrizzlyBears();
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new Shock(), bears, new Shock())));

        harness.activateAbility(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName).contains("Helm of Obedience");
    }

    @Test
    @DisplayName("Milling X non-creature cards leaves the Helm on the battlefield")
    void noCreatureFoundKeepsHelm() {
        harness.addToBattlefield(player1, new HelmOfObedience());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card bears = new GrizzlyBears();
        gd.playerDecks.put(player2.getId(), new ArrayList<>(List.of(new Shock(), new Shock(), bears)));

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears);
        assertThat(findPermanent(player1, "Helm of Obedience")).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An empty library ends the process with no reanimation")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new HelmOfObedience());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.playerDecks.put(player2.getId(), new ArrayList<>());

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(findPermanent(player1, "Helm of Obedience")).isNotNull();
    }
}
