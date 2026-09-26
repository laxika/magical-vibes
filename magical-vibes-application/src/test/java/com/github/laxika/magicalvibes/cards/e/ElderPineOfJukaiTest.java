package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AkkiUnderling;
import com.github.laxika.magicalvibes.cards.g.GhostLitStalker;
import com.github.laxika.magicalvibes.cards.k.KikusShadow;
import com.github.laxika.magicalvibes.cards.m.MikokoroCenterOfTheSea;
import com.github.laxika.magicalvibes.cards.m.MirenTheMoaningWell;
import com.github.laxika.magicalvibes.cards.s.Secretkeeper;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        ElderPineOfJukai.class,
        AkkiUnderling.class,
        GhostLitStalker.class,
        KikusShadow.class,
        MikokoroCenterOfTheSea.class,
        MirenTheMoaningWell.class,
        Secretkeeper.class,
        SpiritualVisit.class
})
class ElderPineOfJukaiTest extends BaseCardTest {

    private void destroyElderPine() {
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, 0,
                harness.getPermanentId(player1, "Elder Pine of Jukai"));
    }

    @Test
    @DisplayName("Casting a Spirit or Arcane spell reveals three cards and puts revealed lands into hand")
    void castTriggerPutsLandsIntoHandAndRestOnBottom() {
        harness.addToBattlefield(player1, new ElderPineOfJukai());
        Card firstLand = new MirenTheMoaningWell();
        Card topNonland = new AkkiUnderling();
        Card secondLand = new MikokoroCenterOfTheSea();
        Card bottomCard = new AkkiUnderling();
        harness.setLibrary(player1, List.of(firstLand, topNonland, secondLand, bottomCard));

        harness.setHand(player1, List.of(new SpiritualVisit()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bottomCard, topNonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Casting a Spirit spell triggers Elder Pine")
    void spiritSpellTriggers() {
        harness.addToBattlefield(player1, new ElderPineOfJukai());
        Card firstLand = new MirenTheMoaningWell();
        Card topNonland = new AkkiUnderling();
        Card secondLand = new MikokoroCenterOfTheSea();
        Card bottomCard = new AkkiUnderling();
        harness.setLibrary(player1, List.of(firstLand, topNonland, secondLand, bottomCard));

        harness.setHand(player1, List.of(new GhostLitStalker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstLand, secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bottomCard, topNonland);
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Elder Pine")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElderPineOfJukai());
        Card libraryCard = new MirenTheMoaningWell();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.setHand(player1, List.of(new AkkiUnderling()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Spirit or Arcane spell cast by an opponent does not trigger Elder Pine")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElderPineOfJukai());
        Card libraryCard = new MirenTheMoaningWell();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.setHand(player2, List.of(new SpiritualVisit()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Soulshift 2 returns a targeted Spirit with mana value 2 or less to hand")
    void soulshiftReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new ElderPineOfJukai());
        Card spirit = new GhostLitStalker();
        harness.setGraveyard(player1, List.of(spirit));

        destroyElderPine();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(spirit);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift only offers a Spirit with mana value 2 or less from its controller's graveyard")
    void soulshiftFiltersGraveyardTargets() {
        harness.addToBattlefield(player1, new ElderPineOfJukai());
        Card eligibleSpirit = new GhostLitStalker();
        Card expensiveSpirit = new Secretkeeper();
        Card nonSpirit = new AkkiUnderling();
        Card opponentSpirit = new GhostLitStalker();
        harness.setGraveyard(player1, List.of(eligibleSpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        destroyElderPine();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(eligibleSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(
                expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new ElderPineOfJukai());
        Card spirit = new GhostLitStalker();
        harness.setGraveyard(player1, List.of(spirit));

        destroyElderPine();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spirit);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(spirit);
    }

    @Test
    @DisplayName("Soulshift presents no choice without a legal target")
    void soulshiftNoLegalTargetDoesNotPrompt() {
        harness.addToBattlefield(player1, new ElderPineOfJukai());
        harness.setGraveyard(player1, List.of(new Secretkeeper(), new AkkiUnderling()));

        destroyElderPine();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
