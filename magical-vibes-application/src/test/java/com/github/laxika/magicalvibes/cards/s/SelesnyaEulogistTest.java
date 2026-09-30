package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals(name))
                .toList();
    }
}
