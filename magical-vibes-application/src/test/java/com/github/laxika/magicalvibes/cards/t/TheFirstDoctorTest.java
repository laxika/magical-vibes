package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BloodbraidElf;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFirstDoctor.class, BloodbraidElf.class, GrizzlyBears.class, Forest.class})
class TheFirstDoctorTest extends BaseCardTest {

    @Test
    void searchesForTardisInLibraryOrGraveyard() {
        Card libraryTardis = namedTardis();
        Card graveyardTardis = namedTardis();
        harness.setLibrary(player1, List.of(libraryTardis));
        harness.setGraveyard(player1, List.of(graveyardTardis));
        harness.setHand(player1, List.of(new TheFirstDoctor()));
        addFirstDoctorMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(libraryTardis.getId(), graveyardTardis.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardTardis.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(graveyardTardis);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryTardis);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardTardis);
    }

    @Test
    void putsCounterOnArtifactOrCreatureWhenCascadeSpellIsCast() {
        harness.addToBattlefield(player1, new TheFirstDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card enchantment = new Card();
        enchantment.setName("Test Enchantment");
        enchantment.setType(CardType.ENCHANTMENT);
        Permanent illegalTarget = harness.addToBattlefieldAndReturn(player1, enchantment);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new BloodbraidElf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, illegalTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    void doesNotTriggerForNonCascadeSpell() {
        harness.addToBattlefield(player1, new TheFirstDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Card namedTardis() {
        Card card = new Card();
        card.setName("TARDIS");
        card.setType(CardType.ARTIFACT);
        return card;
    }

    private void addFirstDoctorMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
