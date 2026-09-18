package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BattlefieldScrounger;
import com.github.laxika.magicalvibes.cards.c.CommanderEesha;
import com.github.laxika.magicalvibes.cards.g.GoretuskFirebeast;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
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

@CardUsed({SuturedGhoul.class, GoretuskFirebeast.class, CommanderEesha.class,
        BattlefieldScrounger.class, MentalNote.class})
class SuturedGhoulTest extends BaseCardTest {

    private void castGhoul() {
        harness.castFromHand(player1, new SuturedGhoul(), "{4}{B}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Power and toughness equal the total power and toughness of the exiled creature cards")
    void powerToughnessSumExiledCards() {
        GoretuskFirebeast firebeast = new GoretuskFirebeast(); // 2/2
        CommanderEesha eesha = new CommanderEesha();           // 2/4
        harness.setGraveyard(player1, List.of(firebeast, eesha));

        castGhoul();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(firebeast.getId(), eesha.getId()));

        Permanent ghoul = findPermanent(player1, "Sutured Ghoul");
        assertThat(harness.getGameQueryService().getEffectivePower(gd, ghoul)).isEqualTo(4);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, ghoul)).isEqualTo(6);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getId())
                .contains(firebeast.getId(), eesha.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen cards are exiled; unchosen creature cards stay in the graveyard")
    void unchosenCardsStayInGraveyard() {
        GoretuskFirebeast firebeast = new GoretuskFirebeast(); // 2/2
        BattlefieldScrounger scrounger = new BattlefieldScrounger(); // 3/3
        harness.setGraveyard(player1, List.of(firebeast, scrounger));

        castGhoul();
        harness.handleMultipleCardsChosen(player1, List.of(scrounger.getId()));

        Permanent ghoul = findPermanent(player1, "Sutured Ghoul");
        assertThat(harness.getGameQueryService().getEffectivePower(gd, ghoul)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, ghoul)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(c -> c.getId())
                .containsExactly(firebeast.getId());
    }

    @Test
    @DisplayName("Exiling nothing leaves a 0/0 that dies to state-based actions")
    void exilingNothingDiesAsZeroZero() {
        GoretuskFirebeast firebeast = new GoretuskFirebeast();
        harness.setGraveyard(player1, List.of(firebeast));

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
        GoretuskFirebeast firebeast = new GoretuskFirebeast();
        harness.setGraveyard(player1, List.of(mentalNote, firebeast));

        castGhoul();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firebeast.getId());
    }

    @Test
    @DisplayName("With an empty graveyard the ghoul enters as a 0/0 with no choice offered")
    void emptyGraveyardNoChoice() {
        castGhoul();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sutured Ghoul"));
    }

    @Test
    @DisplayName("The ghoul cannot exile creature cards from an opponent's graveyard")
    void doesNotUseOpponentsGraveyard() {
        GoretuskFirebeast firebeast = new GoretuskFirebeast();
        harness.setGraveyard(player2, List.of(firebeast));

        castGhoul();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(firebeast);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sutured Ghoul"));
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        BattlefieldScrounger scrounger = new BattlefieldScrounger();
        harness.setGraveyard(player1, List.of(scrounger));

        castGhoul();
        harness.handleMultipleCardsChosen(player1, List.of(scrounger.getId()));

        Permanent ghoul = findPermanent(player1, "Sutured Ghoul");
        ghoul.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new GoretuskFirebeast());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Creature cards exiled with the ghoul remain in exile when it leaves")
    void exiledCardsRemainWhenGhoulLeaves() {
        GoretuskFirebeast firebeast = new GoretuskFirebeast();
        harness.setGraveyard(player1, List.of(firebeast));

        castGhoul();
        harness.handleMultipleCardsChosen(player1, List.of(firebeast.getId()));

        Permanent ghoul = findPermanent(player1, "Sutured Ghoul");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ghoul));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firebeast);
        assertThat(gd.getCardsExiledByPermanent(ghoul.getId())).containsExactly(firebeast);
    }
}
