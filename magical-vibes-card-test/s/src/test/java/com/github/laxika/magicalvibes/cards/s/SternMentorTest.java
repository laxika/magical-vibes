package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SternMentor.class, Vorstclaw.class, SongOfTheDryads.class})
class SternMentorTest extends BaseCardTest {

    private Permanent castAndPairWithPartner() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        harness.setHand(player1, List.of(new SternMentor()));
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, partner.getId());
        return partner;
    }

    private void activateMill(Permanent permanent) {
        permanent.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
        harness.activateAbility(player1, index, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("While paired, Stern Mentor's granted ability mills the target player two cards")
    void pairedMentorMillsTwo() {
        castAndPairWithPartner();
        Permanent mentor = findPermanent(player1, "Stern Mentor");

        List<Card> deck = gd.playerDecks.get(player2.getId());
        int sizeBefore = deck.size();
        List<Card> topTwo = List.copyOf(deck.subList(0, 2));

        activateMill(mentor);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(sizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(topTwo);
        assertThat(findPermanent(player1, "Stern Mentor").isTapped()).isTrue();
    }

    @Test
    @DisplayName("While paired, the partner also has the mill ability")
    void pairedPartnerMillsTwo() {
        Permanent partner = castAndPairWithPartner();

        int sizeBefore = gd.playerDecks.get(player2.getId()).size();

        activateMill(partner);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(sizeBefore - 2);
        assertThat(partner.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unpaired Stern Mentor does not have the mill ability")
    void unpairedHasNoMillAbility() {
        harness.addToBattlefield(player1, new SternMentor());
        Permanent mentor = findPermanent(player1, "Stern Mentor");
        mentor.setSummoningSick(false);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mentor);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enteringWithoutEligiblePartnerDoesNotTriggerSoulbond() {
        harness.setHand(player1, List.of(new SternMentor()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayDeclinePairing() {
        Permanent partner = harness.addToBattlefieldAndReturn(player1, new Vorstclaw());
        harness.setHand(player1, List.of(new SternMentor()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(partner.getPairedWithId()).isNull();
        assertThat(findPermanent(player1, "Stern Mentor").getPairedWithId()).isNull();
        partner.setSummoningSick(false);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pairsWithLaterEnteringCreature() {
        Permanent mentor = harness.addToBattlefieldAndReturn(player1, new SternMentor());
        harness.setHand(player1, List.of(new Vorstclaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        Permanent partner = findPermanent(player1, "Vorstclaw");

        assertThat(mentor.getPairedWithId()).isEqualTo(partner.getId());
        assertThat(partner.getPairedWithId()).isEqualTo(mentor.getId());
        int before = gd.playerDecks.get(player2.getId()).size();
        activateMill(partner);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(before - 2);
    }

    @Test
    void millCanTargetControllerWithOnlyOneCardRemaining() {
        Permanent partner = castAndPairWithPartner();
        Card lastCard = new Vorstclaw();
        harness.setLibrary(player1, List.of(lastCard));
        partner.setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lastCard);
    }

    @Test
    void newlyEnteredMentorCannotPayTapCost() {
        castAndPairWithPartner();
        Permanent mentor = findPermanent(player1, "Stern Mentor");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mentor);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(mentor.isTapped()).isFalse();
    }

    @Test
    void becomingLandBreaksPairAndRemovesMentorsMillAbility() {
        Permanent partner = castAndPairWithPartner();
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, partner.getId());
        harness.passBothPriorities();
        Permanent mentor = findPermanent(player1, "Stern Mentor");
        mentor.setSummoningSick(false);

        assertThat(gqs.isCreature(gd, partner)).isFalse();
        assertThat(partner.getPairedWithId()).isNull();
        assertThat(mentor.getPairedWithId()).isNull();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mentor);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
