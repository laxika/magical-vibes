package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VastlandsScavengerBindToLife.class, GrizzlyBears.class, Forest.class, LeylineOfTheVoid.class})
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

    @Test
    @DisplayName("Scavenger enters prepared without a preparation trigger on the stack")
    void entersPreparedWithoutTrigger() {
        VastlandsScavengerBindToLife scavenger = new VastlandsScavengerBindToLife();
        harness.setHand(player1, List.of(scavenger));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Vastlands Scavenger");
        assertThat(permanent.isPrepared()).isTrue();
        assertThat(permanent.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Bind to Life immediately after entry cannot prepare Scavenger again")
    void cannotReprepareFromSpuriousEntryTrigger() {
        VastlandsScavengerBindToLife scavenger = new VastlandsScavengerBindToLife();
        harness.setHand(player1, List.of(scavenger));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent permanent = findPermanent(player1, "Vastlands Scavenger");
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castFromExile(player1, permanent.getPreparedSpellCardId());
        assertThat(permanent.isPrepared()).isFalse();
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(permanent.isPrepared()).isFalse();
        assertThat(permanent.getPreparedSpellCardId()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bind to Life mills a short library and returns a preparation creature prepared")
    void shortLibraryReturnsScavengerPrepared() {
        castPreparedScavenger();
        Card creature = new VastlandsScavengerBindToLife();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));

        castPrepareSpell();

        Permanent returned = findBattlefieldCard(creature);
        assertThat(returned).isNotNull();
        assertThat(returned.isPrepared()).isTrue();
        assertThat(returned.getPreparedSpellCardId()).isNotNull();
        assertThat(returned.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(land.getId());
    }

    @Test
    @DisplayName("Bind to Life cannot return a creature already in the graveyard")
    void ignoresCreatureAlreadyInGraveyard() {
        castPreparedScavenger();
        Card oldCreature = new VastlandsScavengerBindToLife();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player1, List.of(new Forest()));

        castPrepareSpell();

        assertThat(findBattlefieldCard(oldCreature)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(oldCreature.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bind to Life resolves with an empty library without returning old creatures")
    void emptyLibrary() {
        castPreparedScavenger();
        Card oldCreature = new VastlandsScavengerBindToLife();
        harness.setGraveyard(player1, List.of(oldCreature));
        harness.setLibrary(player1, List.of());

        castPrepareSpell();

        assertThat(findBattlefieldCard(oldCreature)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(oldCreature.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bind to Life can return a milled creature exiled by Leyline of the Void")
    void returnsCreatureDivertedToExile() {
        castPreparedScavenger();
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        Card creature = new VastlandsScavengerBindToLife();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));

        castPrepareSpell();
        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        if (choice != null) {
            int index = choice.cardPool().stream().map(Card::getId).toList().indexOf(creature.getId());
            assertThat(index).isGreaterThanOrEqualTo(0);
            harness.handleGraveyardCardChosen(player1, index);
        }

        assertThat(findBattlefieldCard(creature)).isNotNull();
        assertThat(gd.findExiledCard(creature.getId())).isNull();
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private Permanent castPreparedScavenger() {
        VastlandsScavengerBindToLife scavenger = new VastlandsScavengerBindToLife();
        harness.setHand(player1, List.of(scavenger));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Vastlands Scavenger");
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
