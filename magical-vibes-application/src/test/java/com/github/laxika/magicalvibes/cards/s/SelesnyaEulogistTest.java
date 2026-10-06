package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelesnyaEulogist.class, GrizzlyBears.class, Forest.class})
class SelesnyaEulogistTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card, then populates")
    void exilesCreatureAndPopulates() {
        addCreatureReady(player1, new SelesnyaEulogist());
        addCreatureToken(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        addMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(creatureTokensNamed(player1, "Grizzly Bears")).hasSize(2);
    }

    @Test
    @DisplayName("Exiles the creature card without populating when there is no creature token")
    void exilesWithoutCreatureToken() {
        addCreatureReady(player1, new SelesnyaEulogist());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        addMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(creatureTokensNamed(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void rejectsNonCreatureGraveyardTarget() {
        addCreatureReady(player1, new SelesnyaEulogist());
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));
        addMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canExileFromOwnGraveyardWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SelesnyaEulogist());
        source.setTapped(true);
        source.setSummoningSick(true);
        addCreatureToken(player1);
        Card creature = new SelesnyaEulogist();
        harness.setGraveyard(player1, List.of(creature));
        addMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Selesnya Eulogist");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(creatureTokensNamed(player1, "Grizzly Bears")).hasSize(2);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void doesNotPopulateWhenTargetLeavesGraveyard() {
        addCreatureReady(player1, new SelesnyaEulogist());
        addCreatureToken(player1);
        Card creature = new SelesnyaEulogist();
        harness.setGraveyard(player2, List.of(creature));
        addMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(creature);
        assertThat(creatureTokensNamed(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new SelesnyaEulogist());
        addCreatureToken(player1);
        Card creature = new SelesnyaEulogist();
        harness.setGraveyard(player2, List.of(creature));
        addMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.setGraveyard(player1, List.of(source.getCard()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(creatureTokensNamed(player1, "Grizzly Bears")).hasSize(2);
    }

    @Test
    void ignoresOpponentsCreatureTokensAndOwnNoncreatureTokens() {
        addCreatureReady(player1, new SelesnyaEulogist());
        addCreatureToken(player2);
        Card landToken = new Forest();
        landToken.setToken(true);
        harness.addToBattlefield(player1, landToken);
        Card creature = new SelesnyaEulogist();
        harness.setGraveyard(player2, List.of(creature));
        addMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(creatureTokensNamed(player2, "Grizzly Bears")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void choosesTokenAtResolutionAfterExilingTarget() {
        addCreatureReady(player1, new SelesnyaEulogist());
        Permanent firstToken = addCreatureToken(player1);
        Card creature = new SelesnyaEulogist();
        harness.setGraveyard(player2, List.of(creature));
        addMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        Card token = new SelesnyaEulogist();
        token.setToken(true);
        Permanent secondToken = harness.addToBattlefieldAndReturn(player1, token);
        addCreatureToken(player2);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        harness.assertNotInGraveyard(player2, "Selesnya Eulogist");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(firstToken.getId(), secondToken.getId());
        harness.handlePermanentChosen(player1, secondToken.getId());

        assertThat(creatureTokensNamed(player1, "Selesnya Eulogist")).hasSize(2);
        assertThat(creatureTokensNamed(player1, "Grizzly Bears")).hasSize(1);
        assertThat(creatureTokensNamed(player2, "Grizzly Bears")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Permanent addCreatureToken(Player player) {
        Card token = new GrizzlyBears();
        token.setToken(true);
        return addCreatureReady(player, token);
    }

    private List<Permanent> creatureTokensNamed(Player player, String name) {
        return findPermanents(player, name).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
