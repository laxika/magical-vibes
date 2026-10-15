package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BallyrushBanneret;
import com.github.laxika.magicalvibes.cards.b.BoskBanneret;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.h.HuntingTriad;
import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolfSkullShaman.class, ElvishWarrior.class, BallyrushBanneret.class,
        BoskBanneret.class, MothdustChangeling.class, HuntingTriad.class})
class WolfSkullShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new WolfSkullShaman());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        acceptLookIfOffered();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Revealing the shared-type card creates a 2/2 green Wolf token")
    void revealCreatesWolfToken() {
        addCreatureReady(player1, new WolfSkullShaman());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        acceptLookIfOffered();
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> tokens = getTokens(player1);
        assertThat(tokens).hasSize(1);

        Permanent wolf = tokens.getFirst();
        assertThat(wolf.getCard().getPower()).isEqualTo(2);
        assertThat(wolf.getCard().getToughness()).isEqualTo(2);
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(getTokens(player2)).isEmpty();
        assertThat(wolf.getCard().getSubtypes()).containsExactly(CardSubtype.WOLF);
    }

    @Test
    @DisplayName("Declining to reveal creates no token")
    void decliningCreatesNoToken() {
        addCreatureReady(player1, new WolfSkullShaman());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        acceptLookIfOffered();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(getTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new WolfSkullShaman());
        harness.setLibrary(player1, List.of(new BallyrushBanneret()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        acceptLookIfOffered();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(getTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Looking at the top card can be declined even when no creature type matches")
    void mayDeclineLookingAtNonmatchingCard() {
        addCreatureReady(player1, new WolfSkullShaman());
        BallyrushBanneret topCard = new BallyrushBanneret();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gameLogContains("looks at the top card")).isFalse();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(getTokens(player1)).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gameLogContains("looks at the top card")).isFalse();
    }

    @Test
    @DisplayName("Kinship still offers a reveal after its source leaves the battlefield")
    void sourceLeavesBeforeResolution() {
        Permanent shaman = addCreatureReady(player1, new WolfSkullShaman());
        ElvishWarrior topCard = new ElvishWarrior();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(shaman);
        gd.playerGraveyards.get(player1.getId()).add(shaman.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        acceptLookIfOffered();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(getTokens(player1)).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Sharing only Shaman is enough for kinship")
    void sharedShamanTypeCreatesToken() {
        addCreatureReady(player1, new WolfSkullShaman());
        BoskBanneret topCard = new BoskBanneret();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        acceptLookIfOffered();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(getTokens(player1)).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("A changeling on top shares a creature type with the Shaman")
    void changelingCreatesToken() {
        addCreatureReady(player1, new WolfSkullShaman());
        MothdustChangeling topCard = new MothdustChangeling();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        acceptLookIfOffered();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(getTokens(player1)).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An empty library cannot produce a Wolf")
    void emptyLibraryCreatesNoToken() {
        addCreatureReady(player1, new WolfSkullShaman());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(getTokens(player1)).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Kinship does not trigger on an opponent's upkeep")
    void opponentUpkeepDoesNotTriggerKinship() {
        addCreatureReady(player1, new WolfSkullShaman());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(getTokens(player1)).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("looks at the top card")).isFalse();
    }

    @Test
    @DisplayName("A noncreature kindred card can share a creature type for kinship")
    void kindredSorceryCreatesToken() {
        addCreatureReady(player1, new WolfSkullShaman());
        HuntingTriad topCard = new HuntingTriad();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        acceptLookIfOffered();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(getTokens(player1)).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
    private void acceptLookIfOffered() {
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        if (choice != null && choice.description().toLowerCase(java.util.Locale.ROOT).contains("look")) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }

    private List<Permanent> getTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }
}
