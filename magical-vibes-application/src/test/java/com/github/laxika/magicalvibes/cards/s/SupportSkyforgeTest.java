package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AethersphereHarvester;
import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartOfKiran;
import com.github.laxika.magicalvibes.cards.h.HighSpeedHoverbike;
import com.github.laxika.magicalvibes.cards.h.Hulldrifter;
import com.github.laxika.magicalvibes.cards.s.SkySkiff;
import com.github.laxika.magicalvibes.cards.s.SkysovereignConsulFlagship;
import com.github.laxika.magicalvibes.cards.s.SmugglersCopter;
import com.github.laxika.magicalvibes.cards.y.SupportSkyforge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        SupportSkyforge.class,
        HeartOfKiran.class,
        HighSpeedHoverbike.class,
        SkySkiff.class,
        SmugglersCopter.class,
        AethersphereHarvester.class,
        AirResponseUnit.class,
        Hulldrifter.class,
        SkysovereignConsulFlagship.class,
        GrizzlyBears.class
})
class SupportSkyforgeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four 1/1 colorless Servo artifact creature tokens")
    void createsFourServosOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new SupportSkyforge());
        harness.passBothPriorities();

        List<Permanent> servos = findPermanents(player1, "Servo");
        assertThat(servos).hasSize(4);
        assertThat(servos).allSatisfy(servo -> {
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().getPower()).isEqualTo(1);
            assertThat(servo.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Attacking drafts three spellbook cards and makes the selected card an artifact creature")
    void attacksDraftAndPerpetuallyChangesSelectedCard() {
        Permanent skyforge = harness.addToBattlefieldAndReturn(player1, new SupportSkyforge());
        skyforge.setSummoningSick(false);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);

        Card selected = choice.allCards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(selected);
        assertThat(selected.hasType(CardType.ARTIFACT)).isTrue();
        assertThat(selected.hasType(CardType.CREATURE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
