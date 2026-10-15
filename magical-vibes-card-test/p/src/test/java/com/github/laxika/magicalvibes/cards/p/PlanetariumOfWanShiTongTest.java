package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DrannithMagistrate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KishlaVillage;
import com.github.laxika.magicalvibes.cards.o.OtterPenguin;
import com.github.laxika.magicalvibes.cards.v.VillageRites;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanetariumOfWanShiTong.class, GrizzlyBears.class, Forest.class, KishlaVillage.class,
        OtterPenguin.class, VillageRites.class, DrannithMagistrate.class})
class PlanetariumOfWanShiTongTest extends BaseCardTest {

    @Test
    @DisplayName("Scrying can cast a creature from the top of the library without mana")
    void scryTriggerCastsTopCreatureForFree() {
        harness.addToBattlefield(player1, new PlanetariumOfWanShiTong());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        answerScry(List.of(0, 1), List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Declining the surveil trigger leaves it available later that turn")
    void decliningSurveilTriggerDoesNotUseIt() {
        harness.addToBattlefield(player1, new PlanetariumOfWanShiTong());
        Permanent firstVillage = addReadyVillage();
        Permanent secondVillage = addReadyVillage();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        addVillageMana(2);

        surveilWith(firstVillage);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        surveilWith(secondVillage);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Choosing to cast consumes the trigger for the rest of the turn")
    void acceptingSurveilTriggerUsesItForTheTurn() {
        harness.addToBattlefield(player1, new PlanetariumOfWanShiTong());
        Permanent firstVillage = addReadyVillage();
        Permanent secondVillage = addReadyVillage();
        Card firstTopCard = new GrizzlyBears();
        Card secondTopCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstTopCard, secondTopCard, new Forest()));
        addVillageMana(2);

        surveilWith(firstVillage);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        surveilWith(secondVillage);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(secondTopCard);
    }

    @Test
    @DisplayName("The card offered for casting is the top card after scrying")
    void castsCardMovedToTopByScry() {
        harness.addToBattlefield(player1, new PlanetariumOfWanShiTong());
        Card creature = new OtterPenguin();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        answerScry(List.of(1, 0), List.of());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Otter-Penguin");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("A land on top cannot be played by the cast permission")
    void landOnTopRemainsInLibrary() {
        harness.addToBattlefield(player1, new PlanetariumOfWanShiTong());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land, new OtterPenguin()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        answerScry(List.of(0, 1), List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(land);
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Surveiling every remaining card into the graveyard leaves nothing to cast")
    void emptyLibraryAfterSurveilDoesNothing() {
        harness.addToBattlefield(player1, new PlanetariumOfWanShiTong());
        Permanent village = addReadyVillage();
        Card creature = new OtterPenguin();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        addVillageMana(1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(village), 1, null, null);
        harness.passBothPriorities();
        answerScry(List.of(), List.of(0, 1));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature, land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Casting after scry also consumes the surveil permission that turn")
    void scryAndSurveilShareTheOncePerTurnLimit() {
        harness.addToBattlefield(player1, new PlanetariumOfWanShiTong());
        Permanent village = addReadyVillage();
        Card secondCreature = new OtterPenguin();
        harness.setLibrary(player1, List.of(new OtterPenguin(), secondCreature, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        addVillageMana(1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        answerScry(List.of(0, 1), List.of());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        surveilWith(village);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(secondCreature);
    }

    @Test
    @DisplayName("Triggers already on the stack can each permit a cast")
    void pendingTriggersCanEachCast() {
        harness.addToBattlefield(player1, new PlanetariumOfWanShiTong());
        Permanent firstVillage = addReadyVillage();
        Permanent secondVillage = addReadyVillage();
        Card secondCreature = new OtterPenguin();
        harness.setLibrary(player1, List.of(new OtterPenguin(), secondCreature, new Forest()));
        addVillageMana(2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(firstVillage), 1, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(secondVillage), 1, null, null);
        harness.passBothPriorities();
        answerScry(List.of(0, 1), List.of());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(secondCreature);
        assertThat(countPermanents(player1, "Otter-Penguin")).isEqualTo(2);
    }

    @Test
    @DisplayName("Free casting still requires mandatory additional costs")
    void cannotCastSacrificeSpellWithoutCreature() {
        harness.addToBattlefield(player1, new PlanetariumOfWanShiTong());
        Card spell = new VillageRites();
        harness.setLibrary(player1, List.of(spell, new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        answerScry(List.of(0, 1), List.of());
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(spell);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == spell);
    }

    @Test
    @DisplayName("Drannith Magistrate prevents casting from the library")
    void libraryCastingRespectsOpponentMagistrate() {
        harness.addToBattlefield(player1, new PlanetariumOfWanShiTong());
        harness.addToBattlefield(player2, new DrannithMagistrate());
        Card creature = new OtterPenguin();
        harness.setLibrary(player1, List.of(creature, new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        answerScry(List.of(0, 1), List.of());
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == creature);
        harness.assertNotOnBattlefield(player1, "Otter-Penguin");
    }

    private Permanent addReadyVillage() {
        Permanent village = harness.addToBattlefieldAndReturn(player1, new KishlaVillage());
        village.untap();
        return village;
    }

    private void addVillageMana(int activations) {
        harness.addMana(player1, ManaColor.GREEN, activations);
        harness.addMana(player1, ManaColor.COLORLESS, activations * 3);
    }

    private void surveilWith(Permanent village) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(village), 1, null, null);
        harness.passBothPriorities();
        answerScry(List.of(0, 1), List.of());
    }

    private void answerScry(List<Integer> topOrder, List<Integer> graveyardOrder) {
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(topOrder, graveyardOrder));
        harness.passBothPriorities();
    }
}
