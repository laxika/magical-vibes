package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.h.HarvesterDruid;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuturedGhoul.class, SuntailHawk.class, GiantWarthog.class, MentalNote.class, HarvesterDruid.class})
class SuturedGhoulTest extends BaseCardTest {

    private void castGhoul() {
        harness.setHand(player1, List.of(new SuturedGhoul()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Power and toughness equal the total power and toughness of the exiled creature cards")
    void powerToughnessSumExiledCards() {
        SuntailHawk hawk = new SuntailHawk();
        GiantWarthog warthog = new GiantWarthog();
        harness.setGraveyard(player1, List.of(hawk, warthog));

        castGhoul();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(hawk.getId(), warthog.getId()));

        Permanent ghoul = findPermanent(player1, "Sutured Ghoul");
        assertThat(harness.getGameQueryService().getEffectivePower(gd, ghoul)).isEqualTo(6);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, ghoul)).isEqualTo(6);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getId())
                .contains(hawk.getId(), warthog.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen cards are exiled; unchosen creature cards stay in the graveyard")
    void unchosenCardsStayInGraveyard() {
        SuntailHawk hawk = new SuntailHawk();
        GiantWarthog warthog = new GiantWarthog();
        harness.setGraveyard(player1, List.of(hawk, warthog));

        castGhoul();
        harness.handleMultipleCardsChosen(player1, List.of(warthog.getId()));

        Permanent ghoul = findPermanent(player1, "Sutured Ghoul");
        assertThat(harness.getGameQueryService().getEffectivePower(gd, ghoul)).isEqualTo(5);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, ghoul)).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getId())
                .containsExactly(hawk.getId());
    }

    @Test
    @DisplayName("Exiling nothing leaves a 0/0 that dies to state-based actions")
    void exilingNothingDiesAsZeroZero() {
        SuntailHawk hawk = new SuntailHawk();
        harness.setGraveyard(player1, List.of(hawk));

        castGhoul();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sutured Ghoul"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sutured Ghoul"));
    }

    @Test
    @DisplayName("Noncreature cards in the graveyard can't be exiled")
    void noncreatureCardsAreNotOffered() {
        MentalNote mentalNote = new MentalNote();
        SuntailHawk hawk = new SuntailHawk();
        harness.setGraveyard(player1, List.of(mentalNote, hawk));

        castGhoul();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(hawk.getId());
    }

    @Test
    @DisplayName("The ghoul only considers creature cards in its controller's graveyard")
    void onlyControllersGraveyardIsConsidered() {
        GiantWarthog warthog = new GiantWarthog();
        harness.setGraveyard(player2, List.of(warthog));

        castGhoul();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sutured Ghoul"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(c -> c.getId())
                .containsExactly(warthog.getId());
    }

    @Test
    @DisplayName("Trample deals excess combat damage after Sutured Ghoul enters with a power greater than the blocker")
    void trampleDealsExcessCombatDamage() {
        SuntailHawk hawk = new SuntailHawk();
        GiantWarthog warthog = new GiantWarthog();
        harness.setGraveyard(player1, List.of(hawk, warthog));

        castGhoul();
        harness.handleMultipleCardsChosen(player1, List.of(hawk.getId(), warthog.getId()));

        Permanent ghoul = findPermanent(player1, "Sutured Ghoul");
        ghoul.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new HarvesterDruid());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 5
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ghoul);
    }

    @Test
    @DisplayName("With an empty graveyard the ghoul enters as a 0/0 with no choice offered")
    void emptyGraveyardNoChoice() {
        castGhoul();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sutured Ghoul"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sutured Ghoul"));
    }
}
