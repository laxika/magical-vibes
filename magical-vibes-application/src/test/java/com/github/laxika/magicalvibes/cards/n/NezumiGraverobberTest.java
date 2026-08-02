package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NezumiGraverobberTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the targeted card and flips when that graveyard is left empty")
    void exilesLastCardAndFlips() {
        Permanent graverobber = addReadyGraverobber(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
        assertThat(graverobber.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip while cards remain in that graveyard")
    void doesNotFlipWhenGraveyardStillHasCards() {
        Permanent graverobber = addReadyGraverobber(player1);
        Card bears = new GrizzlyBears();
        Card bolt = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears, bolt)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId).containsExactly(bolt.getId());
        assertThat(graverobber.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a card in the controller's own graveyard")
    void cannotTargetOwnGraveyard() {
        Permanent graverobber = addReadyGraverobber(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, index, 0,
                List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's graveyard");
    }

    @Test
    @DisplayName("Flipped side reanimates target creature card from any graveyard under your control")
    void flippedSideReanimatesFromAnyGraveyard() {
        Permanent graverobber = addReadyGraverobber(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(graverobber.isTransformed()).isTrue();

        Card zombie = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(zombie)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        index = gd.playerBattlefields.get(player1.getId()).indexOf(graverobber);
        harness.activateAbility(player1, index, 0, null, zombie.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(zombie.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addReadyGraverobber(Player player) {
        Permanent perm = new Permanent(new NezumiGraverobber());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
