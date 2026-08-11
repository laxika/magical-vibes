package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FamishedGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and exiles two cards from one graveyard")
    void sacrificesSelfAndExilesTwoCards() {
        Permanent ghoul = addReadyGhoul(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, ghoulIndex(ghoul), 0,
                List.of(card1.getId(), card2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ghoul);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ghoul.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId).containsExactlyInAnyOrder(card1.getId(), card2.getId());
    }

    @Test
    @DisplayName("Can exile only one card from the controller's graveyard")
    void exilesFewerCardsFromOwnGraveyard() {
        Permanent ghoul = addReadyGhoul(player1);
        Card chosen = new GrizzlyBears();
        Card remaining = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(chosen, remaining)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, ghoulIndex(ghoul), 0,
                List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactlyInAnyOrder(remaining.getId(), ghoul.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(chosen.getId());
    }

    @Test
    @DisplayName("Targets must all come from a single graveyard")
    void targetsMustShareOneGraveyard() {
        Permanent ghoul = addReadyGhoul(player1);
        Card mine = new GrizzlyBears();
        Card theirs = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(mine)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(theirs)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, ghoulIndex(ghoul), 0,
                List.of(mine.getId(), theirs.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent ghoul = addReadyGhoul(player1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, ghoulIndex(ghoul), 0,
                List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private int ghoulIndex(Permanent ghoul) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);
    }

    private Permanent addReadyGhoul(Player player) {
        Permanent perm = new Permanent(new FamishedGhoul());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
