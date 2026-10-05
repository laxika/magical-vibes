package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingLore.class, Divination.class, GrizzlyBears.class, LightningBolt.class})
class LivingLoreTest extends BaseCardTest {

    @Test
    void exilesExactlyOneInstantOrSorceryAndUsesItsManaValue() {
        Card creature = new GrizzlyBears();
        Card instant = new LightningBolt();
        Card sorcery = new Divination();
        castLivingLore(List.of(creature, instant, sorcery));

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));

        Permanent lore = findPermanent(player1, "Living Lore");
        assertThat(harness.getGameQueryService().getEffectivePower(gd, lore)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, lore)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId(), instant.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(sorcery.getId());
    }

    @Test
    void noMatchingCardLeavesLivingLoreAsZeroZero() {
        Card creature = new GrizzlyBears();
        LivingLore loreCard = castLivingLore(List.of(creature));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(loreCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(loreCard.getId()));
    }

    @Test
    void combatDamageMaySacrificeLivingLoreAndCastTheExiledCardForFree() {
        Divination divination = new Divination();
        castLivingLore(List.of(divination));
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        Permanent lore = findPermanent(player1, "Living Lore");
        lore.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(lore.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(divination.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(divination.getId()));
    }

    @Test
    void decliningSacrificeKeepsLivingLoreAndTheExiledCard() {
        Divination divination = new Divination();
        castLivingLore(List.of(divination));
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        Permanent lore = findPermanent(player1, "Living Lore");
        lore.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(lore);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(divination.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void maySacrificeWithoutCastingTheExiledCard() {
        Divination divination = new Divination();
        castLivingLore(List.of(divination));
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        Permanent lore = findPermanent(player1, "Living Lore");
        lore.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lore);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(lore.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(divination.getId());
    }

    @Test
    void cannotExileAnOpponentsSorceryWhenItsOwnGraveyardHasNoMatchingCard() {
        Divination divination = new Divination();
        harness.setGraveyard(player2, List.of(divination));

        LivingLore lore = castLivingLore(List.of());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(lore.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(divination.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void combatDamageToABlockingCreatureAlsoAllowsSacrifice() {
        Divination divination = new Divination();
        castLivingLore(List.of(divination));
        harness.handleMultipleCardsChosen(player1, List.of(divination.getId()));
        Permanent lore = findPermanent(player1, "Living Lore");
        lore.setSummoningSick(false);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lore);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(divination.getId());
    }

    @Test
    void lethalCombatDamagePreventsSacrificeAndCasting() {
        LightningBolt bolt = new LightningBolt();
        castLivingLore(List.of(bolt));
        harness.handleMultipleCardsChosen(player1, List.of(bolt.getId()));
        Permanent lore = findPermanent(player1, "Living Lore");
        lore.setSummoningSick(false);
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lore);
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).contains(bolt.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(lore.getCard().getId()).doesNotContain(bolt.getId());
    }

    private LivingLore castLivingLore(List<Card> graveyard) {
        LivingLore lore = new LivingLore();
        harness.setGraveyard(player1, new ArrayList<>(graveyard));
        harness.setHand(player1, List.of(lore));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return lore;
    }
}
