package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterTribeRallier.class, GrizzlyBears.class, HillGiant.class, CrawWurm.class, Plains.class, Shock.class})
class WaterTribeRallierTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend taps five creatures and leaves noncreatures untapped")
    void waterbendTapsFiveCreatures() {
        Permanent rallier = harness.addToBattlefieldAndReturn(player1, new WaterTribeRallier());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.activateAbility(player1, 0, null, null);

        assertThat(List.of(rallier, first, second, third, fourth)).allMatch(Permanent::isTapped);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Offers only creature cards with power 3 or less from the top four")
    void offersOnlyEligibleCreatures() {
        Card lowPowerCreature = new GrizzlyBears();
        Card powerThreeCreature = new HillGiant();
        Card highPowerCreature = new CrawWurm();
        Card noncreature = new Shock();
        setLibrary(lowPowerCreature, powerThreeCreature, highPowerCreature, noncreature);
        activateWithCreatures();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrder(lowPowerCreature.getId(), powerThreeCreature.getId());
    }

    @Test
    @DisplayName("Chosen creature goes to hand and the rest go to the library bottom")
    void choosesEligibleCreature() {
        Card chosen = new GrizzlyBears();
        Card otherEligible = new HillGiant();
        Card highPowerCreature = new CrawWurm();
        Card noncreature = new Shock();
        setLibrary(chosen, otherEligible, highPowerCreature, noncreature);
        activateWithCreatures();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(otherEligible, highPowerCreature, noncreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible creature, all four cards go to the library bottom")
    void noEligibleCreature() {
        Card highPowerCreature = new CrawWurm();
        Card firstNoncreature = new Shock();
        Card secondNoncreature = new Shock();
        Card thirdNoncreature = new Shock();
        setLibrary(highPowerCreature, firstNoncreature, secondNoncreature, thirdNoncreature);
        activateWithCreatures();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(highPowerCreature, firstNoncreature, secondNoncreature, thirdNoncreature);
    }

    private void activateWithCreatures() {
        harness.addToBattlefieldAndReturn(player1, new WaterTribeRallier());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
