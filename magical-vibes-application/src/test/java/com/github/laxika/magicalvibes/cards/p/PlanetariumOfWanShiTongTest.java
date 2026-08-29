package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KishlaVillage;
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

@CardUsed({PlanetariumOfWanShiTong.class, GrizzlyBears.class, Forest.class, KishlaVillage.class})
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
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
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
    }
}
