package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NyxWeaverTest extends BaseCardTest {

    @Test
    void millsTwoCardsAtControllerUpkeep() {
        addReadyNyxWeaver(player1);
        Card first = new GrizzlyBears();
        Card second = new LightningBolt();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(graveyardIds(player1)).containsExactly(first.getId(), second.getId());
    }

    @Test
    void exilesSelfAndReturnsOneTargetedCardToHand() {
        Permanent weaver = addReadyNyxWeaver(player1);
        Card target = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(weaver), 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(weaver);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(weaver.getCard().getId()));
    }

    @Test
    void cannotTargetCardInOpponentsGraveyard() {
        Permanent weaver = addReadyNyxWeaver(player1);
        Card target = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(weaver), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyNyxWeaver(Player player) {
        Permanent perm = new Permanent(new NyxWeaver());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private List<UUID> handIds(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(Card::getId).toList();
    }

    private List<UUID> graveyardIds(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(Card::getId).toList();
    }
}
