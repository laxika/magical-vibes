package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AmbitiousFarmhandTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may search puts a basic Plains into hand")
    void etbSearchPutsBasicPlainsInHand() {
        castFarmhand();
        setupLibrary();

        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getSupertypes().contains(CardSupertype.BASIC) && c.getName().equals("Plains"));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Declining the ETB search puts nothing in hand")
    void decliningEtbSearchDoesNothing() {
        castFarmhand();
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Cannot activate coven transform without three different powers")
    void cannotTransformWithoutCoven() {
        Permanent farmhand = addCreatureReady(player1, new AmbitiousFarmhand());
        harness.addToBattlefield(player1, new LlanowarElves()); // 1/1 — same power as farmhand
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(farmhand), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
    }

    @Test
    @DisplayName("Coven transform turns Ambitious Farmhand into Seasoned Cathar")
    void covenTransformsIntoSeasonedCathar() {
        Permanent farmhand = addCreatureReady(player1, new AmbitiousFarmhand());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
        harness.addToBattlefield(player1, new HillGiant()); // 3/3
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, indexOf(farmhand), null, null);
        harness.passBothPriorities();

        assertThat(farmhand.isTransformed()).isTrue();
        assertThat(farmhand.getCard().getName()).isEqualTo("Seasoned Cathar");
    }

    private void castFarmhand() {
        harness.setHand(player1, List.of(new AmbitiousFarmhand()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new GrizzlyBears()));
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
