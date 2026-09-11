package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VastlandsScavengerBindToLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Bind to Life mills seven cards and puts the only milled creature onto the battlefield")
    void millsAndPutsOnlyCreatureOntoBattlefield() {
        castPreparedScavenger();
        Card creature = new GrizzlyBears();
        List<Card> library = List.of(creature, new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);

        castPrepareSpell();

        assertThat(findBattlefieldCard(creature)).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(library.subList(1, library.size()).stream().map(Card::getId).toArray(java.util.UUID[]::new));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bind to Life makes the controller choose among multiple milled creatures")
    void choosesOneOfMultipleMilledCreatures() {
        castPreparedScavenger();
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        List<Card> library = List.of(firstCreature, new Forest(), secondCreature, new Forest(),
                new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);

        castPrepareSpell();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        int secondCreatureIndex = choice.cardPool().stream()
                .map(Card::getId)
                .toList()
                .indexOf(secondCreature.getId());
        harness.handleGraveyardCardChosen(player1, secondCreatureIndex);

        assertThat(findBattlefieldCard(secondCreature)).isNotNull();
        assertThat(findBattlefieldCard(firstCreature)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstCreature.getId());
    }

    @Test
    @DisplayName("Bind to Life does nothing beyond milling when no creature is milled")
    void noCreatureMilled() {
        castPreparedScavenger();
        List<Card> library = List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest());
        harness.setLibrary(player1, library);

        castPrepareSpell();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> library.stream().anyMatch(card ->
                        card.getId().equals(permanent.getCard().getId())));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(library.stream().map(Card::getId).toArray(java.util.UUID[]::new));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent castPreparedScavenger() {
        VastlandsScavengerBindToLife scavenger = new VastlandsScavengerBindToLife();
        harness.setHand(player1, List.of(scavenger));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(scavenger.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(permanent.isPrepared()).isTrue();
        return permanent;
    }

    private void castPrepareSpell() {
        Permanent scavenger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isPrepared())
                .findFirst()
                .orElseThrow();
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castFromExile(player1, scavenger.getPreparedSpellCardId());
        harness.passBothPriorities();

        assertThat(scavenger.isPrepared()).isFalse();
    }

    private Permanent findBattlefieldCard(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
