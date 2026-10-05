package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoboldWarcaller.class, GrizzlyBears.class, Shock.class, Unsummon.class})
class KoboldWarcallerTest extends BaseCardTest {

    @Test
    void perpetuallyGrantsHasteToAChosenCreatureCardInHand() {
        GrizzlyBears bears = new GrizzlyBears();
        addCreatureReady(player1, new KoboldWarcaller());
        harness.setHand(player1, List.of(new Shock(), bears));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PerpetualPowerToughnessChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 1);
        harness.passBothPriorities();

        Permanent enteredBears = findPermanent(player1, "Grizzly Bears");
        assertThat(enteredBears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void hastePersistsAfterReturningToHandAndBeingCastAgain() {
        addCreatureReady(player1, new KoboldWarcaller());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Unsummon()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.assertInHand(player1, "Grizzly Bears");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.HASTE)).isTrue();
        declareAttackers(List.of(1));
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    void choosesFromCurrentHandOnResolutionAndOnlyGrantsHasteToChosenCard() {
        Permanent warcaller = addCreatureReady(player1, new KoboldWarcaller());
        harness.setHand(player1, List.of(new Shock()));
        harness.activateAbility(player1, 0, null, null);
        assertThat(warcaller.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();
        PendingInteraction.PerpetualPowerToughnessChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");
        assertThat(bears).hasSize(2);
        assertThat(bears.get(0).hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(bears.get(1).hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotPromptWhenHandHasNoCreatureCard() {
        addCreatureReady(player1, new KoboldWarcaller());
        harness.setHand(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
