package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderousDebut.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class ThunderousDebutTest extends BaseCardTest {

    @Test
    void withoutBargainPutsUpToTwoCreatureCardsIntoHand() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new HillGiant();
        Card firstNoncreature = new Shock();
        Card secondNoncreature = new Shock();
        setLibrary(firstCreature, firstNoncreature, secondCreature, secondNoncreature);
        harness.setHand(player1, List.of(new ThunderousDebut()));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, secondCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == firstCreature || permanent.getCard() == secondCreature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNoncreature, secondNoncreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bargainedCastPutsUpToTwoCreatureCardsOntoTheBattlefield() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new HillGiant();
        Card firstNoncreature = new Shock();
        Card secondNoncreature = new Shock();
        setLibrary(firstCreature, firstNoncreature, secondCreature, secondNoncreature);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ThunderousDebut()));
        addMana();

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(firstCreature, secondCreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(firstCreature, secondCreature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstNoncreature, secondNoncreature);
        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotBargainBySacrificingACreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new ThunderousDebut()));
        addMana();

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("an artifact, enchantment, or token");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
