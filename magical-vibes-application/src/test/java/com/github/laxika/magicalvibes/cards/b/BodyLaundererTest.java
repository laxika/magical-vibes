package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BodyLaunderer.class, DeathcultRogue.class, Forest.class, GrizzlyBears.class, Shock.class, WrathOfGod.class})
class BodyLaundererTest extends BaseCardTest {

    @Test
    void anotherNontokenCreatureYouControlDiesAndBodyLaundererConnives() {
        Permanent bodyLaunderer = addCreatureReady(player1, new BodyLaunderer());
        Permanent grizzlyBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, grizzlyBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(bodyLaunderer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void deathTriggerReturnsAnotherNonRogueCreatureWithPowerAtMostBodyLaunderersPower() {
        BodyLaunderer bodyLaunderer = new BodyLaunderer();
        Card creature = new GrizzlyBears();
        Card rogue = new DeathcultRogue();
        Card nonCreature = new Forest();
        addCreatureReady(player1, bodyLaunderer);
        harness.setGraveyard(player1, List.of(creature, rogue, nonCreature));
        destroyBodyLaunderer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
        harness.assertInGraveyard(player1, "Body Launderer");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void deathTriggerUsesBodyLaunderersLastKnownPower() {
        BodyLaunderer bodyLaunderer = new BodyLaunderer();
        Permanent bodyPermanent = addCreatureReady(player1, bodyLaunderer);
        bodyPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        GrizzlyBears creature = new GrizzlyBears();
        creature.setPower(4);
        creature.setToughness(4);
        harness.setGraveyard(player1, List.of(creature));
        destroyBodyLaunderer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")).isNotNull();
    }

    private void destroyBodyLaunderer() {
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();
    }
}
