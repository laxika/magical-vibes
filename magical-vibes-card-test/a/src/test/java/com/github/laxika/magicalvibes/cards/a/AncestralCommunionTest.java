package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncestralCommunion.class, EdgarMarkov.class, GrizzlyBears.class, HolyDay.class})
class AncestralCommunionTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentFromGraveyardToHand() {
        Card permanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(permanent));
        harness.setHand(player1, List.of(new AncestralCommunion()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, permanent.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetNonPermanentCard() {
        Card spell = new HolyDay();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(new AncestralCommunion()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void commanderAllowsCopyWithNewGraveyardTarget() {
        Card firstPermanent = new GrizzlyBears();
        Card secondPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstPermanent, secondPermanent));
        harness.setHand(player1, List.of(new AncestralCommunion()));
        Card commander = new EdgarMarkov();
        gd.playerCommanders.get(player1.getId()).add(commander);
        addCreatureReady(player1, commander);
        addMana();

        harness.castSorcery(player1, 0, firstPermanent.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondPermanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstPermanent, secondPermanent);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetOpponentsPermanentCard() {
        Card permanent = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(permanent));
        harness.setHand(player1, List.of(new AncestralCommunion()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnTargetThatLeftGraveyardBeforeResolution() {
        Card permanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(permanent));
        harness.setHand(player1, List.of(new AncestralCommunion()));
        addMana();
        harness.castSorcery(player1, 0, permanent.getId());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(permanent));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(permanent);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void decliningNewTargetStillCreatesMandatoryCopy() {
        Card permanent = new GrizzlyBears();
        Card commander = new EdgarMarkov();
        gd.playerCommanders.get(player1.getId()).add(commander);
        addCreatureReady(player1, commander);
        harness.setGraveyard(player1, List.of(permanent));
        harness.setHand(player1, List.of(new AncestralCommunion()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, permanent.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(permanent);
        harness.assertInGraveyard(player1, "Ancestral Communion");
    }

    @Test
    void sameNamedPermanentIsNotTheCommander() {
        Card commander = new EdgarMarkov();
        gd.playerCommanders.get(player1.getId()).add(commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Card otherEdgar = new EdgarMarkov();
        otherEdgar.setOwnerId(player2.getId());
        addCreatureReady(player1, otherEdgar);
        Card permanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(permanent));
        harness.setHand(player1, List.of(new AncestralCommunion()));
        addMana();

        harness.castSorcery(player1, 0, permanent.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
