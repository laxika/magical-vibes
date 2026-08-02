package com.github.laxika.magicalvibes.cards.s;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulOfInnistradTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefield ability returns three target creature cards from your graveyard to your hand")
    void returnsThreeCreatureCards() {
        Permanent soul = addReadySoul(player1);
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second, third)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbilityWithGraveyardTargets(player1, index(soul), 0,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("\"Up to three\" allows returning fewer creature cards")
    void returnsFewerThanThree() {
        Permanent soul = addReadySoul(player1);
        Card bears = new GrizzlyBears();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears, other)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbilityWithGraveyardTargets(player1, index(soul), 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(bears.getId()).doesNotContain(other.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(other.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in your graveyard")
    void cannotTargetNoncreatureCard() {
        Permanent soul = addReadySoul(player1);
        Card bolt = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bolt)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(soul), 0, List.of(bolt.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(bolt.getId()));
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Permanent soul = addReadySoul(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(soul), 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target more than three creature cards")
    void cannotTargetFourCards() {
        Permanent soul = addReadySoul(player1);
        Card a = new GrizzlyBears();
        Card b = new GrizzlyBears();
        Card c = new GrizzlyBears();
        Card d = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(a, b, c, d)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(soul), 0, List.of(a.getId(), b.getId(), c.getId(), d.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Graveyard ability exiles this card as a cost and returns the targeted creature cards")
    void graveyardAbilityExilesSelfAndReturnsCards() {
        Card soul = new SoulOfInnistrad();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(soul, first, second)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(soul.getId()));
    }

    @Test
    @DisplayName("Graveyard ability may target itself, but that target is gone once the exile cost is paid")
    void graveyardAbilityTargetingItselfFizzlesForThatCard() {
        Card soul = new SoulOfInnistrad();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(soul, bears)));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(soul.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(bears.getId()).doesNotContain(soul.getId());
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(soul.getId()));
    }

    private Permanent addReadySoul(Player player) {
        Permanent perm = new Permanent(new SoulOfInnistrad());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int index(Permanent soul) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(soul);
    }

    private List<UUID> handIds(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getId).toList();
    }
}
