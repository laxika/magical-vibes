package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThwartTheGrave.class, BoggartBrute.class, ExpeditionHealer.class, FaerieMiscreant.class,
        FugitiveWizard.class, GrizzlyBears.class, SoulWarden.class})
class ThwartTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature and an optional party creature simultaneously")
    void returnsCreatureAndPartyCreature() {
        Card creature = new GrizzlyBears();
        Card partyCreature = new ExpeditionHealer();
        harness.setGraveyard(player1, List.of(creature, partyCreature));
        castThwartTheGrave(6);

        choose(creature);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(partyCreature.getId());
        choose(partyCreature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(creature.getId(), partyCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId())
                        || card.getId().equals(partyCreature.getId()));
    }

    @Test
    @DisplayName("The first target may be a non-party creature")
    void firstTargetMayBeNonPartyCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castThwartTheGrave(6);

        choose(creature);
        chooseNothing();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(creature.getId());
    }

    @Test
    @DisplayName("The same party creature may be chosen for both targets")
    void samePartyCreatureMayBeChosenForBothTargets() {
        Card partyCreature = new ExpeditionHealer();
        harness.setGraveyard(player1, List.of(partyCreature));
        castThwartTheGrave(6);

        choose(partyCreature);
        choose(partyCreature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(partyCreature.getId());
    }

    @Test
    @DisplayName("A full party reduces the generic cost by four")
    void fullPartyReducesGenericCostByFour() {
        addFullParty();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ThwartTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot cast without a legal first creature target")
    void requiresFirstCreatureTarget() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new ThwartTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castThwartTheGrave(int mana) {
        harness.setHand(player1, List.of(new ThwartTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, mana);
        harness.castSorcery(player1, 0, 0);
    }

    private void choose(Card card) {
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
    }

    private void chooseNothing() {
        harness.handleMultipleCardsChosen(player1, List.of());
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }
}
