package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlpineGuide.class, Mountain.class, Forest.class, LightningBolt.class})
class AlpineGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Alpine Guide may put a Mountain from the library onto the battlefield tapped")
    void maySearchForMountain() {
        harness.setHand(player1, List.of(new AlpineGuide()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement()
                .satisfies(card -> assertThat(card.getSubtypes()).contains(CardSubtype.MOUNTAIN));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Declining Alpine Guide's search leaves the library unchanged")
    void maySearchCanBeDeclined() {
        harness.setHand(player1, List.of(new AlpineGuide()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLibrary(player1, List.of(new Mountain()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Mountain"));
    }

    @Test
    @DisplayName("When Alpine Guide leaves, its controller sacrifices a Mountain")
    void sacrificesMountainWhenLeavingBattlefield() {
        Permanent guide = addCreatureReady(player1, new AlpineGuide());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, guide.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Alpine Guide");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Alpine Guide must attack when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new AlpineGuide());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Alpine Guide can be declared as an attacker")
    void canAttackWhenAble() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AlpineGuide());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
