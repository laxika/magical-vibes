package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeathriteShamanTest extends BaseCardTest {

    @Test
    void exilesLandAndAddsChosenMana() {
        Permanent shaman = addReadyShaman(player1);
        Card land = new Forest();
        harness.setGraveyard(player2, new ArrayList<>(List.of(land)));

        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 0, null, land.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(land);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void exilesInstantOrSorceryAndEachOpponentLosesLife() {
        Permanent shaman = addReadyShaman(player1);
        Card instant = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(instant)));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 1, null, instant.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(instant);
        harness.assertLife(player2, 18);
    }

    @Test
    void exilesCreatureAndGainsLife() {
        Permanent shaman = addReadyShaman(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(creature)));
        harness.setLife(player1, 10);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 2, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        harness.assertLife(player1, 12);
    }

    @Test
    void rejectsTargetWithWrongCardType() {
        Permanent shaman = addReadyShaman(player1);
        Card instant = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(instant)));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, shaman), 0, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fizzlesWithoutLifeGainWhenCreatureLeavesGraveyard() {
        Permanent shaman = addReadyShaman(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(creature)));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, shaman), 2, null, creature.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(creature);
    }

    private Permanent addReadyShaman(Player player) {
        Permanent shaman = new Permanent(new DeathriteShaman());
        shaman.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(shaman);
        return shaman;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
