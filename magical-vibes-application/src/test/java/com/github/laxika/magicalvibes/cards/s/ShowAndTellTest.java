package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodArmor;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShowAndTell.class, CoralMerfolk.class, Forest.class, WornPowerstone.class,
        GloriousAnthem.class, BlanchwoodArmor.class, Disenchant.class})
class ShowAndTellTest extends BaseCardTest {

    @Test
    void eachPlayerChoosesBeforeCardsEnterTogether() {
        CoralMerfolk creature = new CoralMerfolk();
        Forest land = new Forest();
        harness.setHand(player1, List.of(new ShowAndTell(), creature));
        harness.setHand(player2, List.of(land));
        castShowAndTell();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        PendingInteraction.EachPlayerMayPutCardFromHandChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());

        harness.handleMultipleCardsChosen(player2, List.of(land.getId()));

        assertThat(countPermanents(player1, "Coral Merfolk")).isEqualTo(1);
        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void decliningLeavesTheChosenCardInHandAndStillPlacesOtherChoice() {
        CoralMerfolk creature = new CoralMerfolk();
        Forest land = new Forest();
        harness.setHand(player1, List.of(new ShowAndTell(), creature));
        harness.setHand(player2, List.of(land));
        castShowAndTell();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(countPermanents(player1, "Coral Merfolk")).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName).containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void nonPermanentCardsAreNotEligible() {
        harness.setHand(player1, List.of(new ShowAndTell(), new Disenchant()));
        harness.setHand(player2, List.of());
        castShowAndTell();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Disenchant");
        harness.assertInGraveyard(player1, "Show and Tell");
    }

    @Test
    void offersArtifactCreatureEnchantmentAndLandCards() {
        WornPowerstone artifact = new WornPowerstone();
        CoralMerfolk creature = new CoralMerfolk();
        GloriousAnthem enchantment = new GloriousAnthem();
        Forest land = new Forest();
        Disenchant instant = new Disenchant();
        harness.setHand(player1, List.of(new ShowAndTell(), artifact, creature, enchantment, land, instant));
        harness.setHand(player2, List.of());
        castShowAndTell();

        PendingInteraction.EachPlayerMayPutCardFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.EachPlayerMayPutCardFromHandChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                artifact.getId(), creature.getId(), enchantment.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Worn Powerstone", "Coral Merfolk", "Forest", "Disenchant");
    }

    @Test
    void putsAnAuraOntoTheBattlefieldAttachedToAChosenCreature() {
        CoralMerfolk creature = new CoralMerfolk();
        BlanchwoodArmor aura = new BlanchwoodArmor();
        harness.addToBattlefield(player1, creature);
        harness.setHand(player1, List.of(new ShowAndTell(), aura));
        harness.setHand(player2, List.of());
        castShowAndTell();

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(findPermanent(player1, "Blanchwood Armor").getAttachedTo()).isEqualTo(findPermanent(player1, "Coral Merfolk").getId());
    }

    private void castShowAndTell() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    void choosesAuraAttachmentBeforeEitherPlayersCardEnters() {
        var first = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        var second = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());
        Card aura = new BlanchwoodArmor();
        Card land = new Forest();
        harness.setHand(player1, List.of(new ShowAndTell(), aura));
        harness.setHand(player2, List.of(land));
        castShowAndTell();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.handleMultipleCardsChosen(player2, List.of(land.getId()));

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(findPermanent(player1, "Blanchwood Armor").getAttachedTo()).isEqualTo(second.getId());
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void auraCannotAttachToCreatureEnteringInTheSameBatch() {
        Card aura = new BlanchwoodArmor();
        Card creature = new CoralMerfolk();
        harness.setHand(player1, List.of(new ShowAndTell(), aura));
        harness.setHand(player2, List.of(creature));
        castShowAndTell();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.handleMultipleCardsChosen(player2, List.of(creature.getId()));

        harness.assertOnBattlefield(player2, "Coral Merfolk");
        harness.assertNotOnBattlefield(player1, "Blanchwood Armor");
        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
    }
}
