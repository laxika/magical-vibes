package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DesecrationPlague;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NyxWeaver.class, DesecrationPlague.class})
class NyxWeaverTest extends BaseCardTest {

    @Test
    void millsTwoCardsAtControllerUpkeep() {
        addReadyNyxWeaver(player1);
        Card first = new NyxWeaver();
        Card second = new DesecrationPlague();
        harness.setLibrary(player1, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(graveyardIds(player1)).containsExactly(first.getId(), second.getId());
    }

    @Test
    void exilesSelfAndReturnsOneTargetedCardToHand() {
        Permanent weaver = addReadyNyxWeaver(player1);
        Card target = new DesecrationPlague();
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
        Card target = new DesecrationPlague();
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(weaver), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void millsOnlyRemainingCardInShortLibrary() {
        addReadyNyxWeaver(player1);
        Card remaining = new NyxWeaver();
        harness.setLibrary(player1, List.of(remaining));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(graveyardIds(player1)).containsExactly(remaining.getId());
    }

    @Test
    void doesNotMillAtOpponentsUpkeep() {
        addReadyNyxWeaver(player1);
        Card first = new NyxWeaver();
        Card second = new NyxWeaver();
        harness.setLibrary(player1, List.of(first, second));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void canActivateWhileSummoningSickAndPaysExileBeforeResolution() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new NyxWeaver());
        weaver.setSummoningSick(true);
        Card target = new NyxWeaver();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(weaver), 0, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(weaver);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(weaver.getCard().getId()));
        assertThat(graveyardIds(player1)).contains(target.getId());
        assertThat(handIds(player1)).doesNotContain(target.getId());

        harness.passBothPriorities();

        assertThat(handIds(player1)).contains(target.getId());
        assertThat(graveyardIds(player1)).doesNotContain(target.getId());
    }

    @Test
    void removedTargetIsNotReturnedAndExileCostIsNotRefunded() {
        Permanent weaver = addReadyNyxWeaver(player1);
        Card target = new NyxWeaver();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(weaver), 0, List.of(target.getId()));
        harness.setGraveyard(player1, new ArrayList<>());

        harness.passBothPriorities();

        assertThat(handIds(player1)).doesNotContain(target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(weaver);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(weaver.getCard().getId()));
    }

    @Test
    void cannotActivateWithoutRequiredTarget() {
        Permanent weaver = addReadyNyxWeaver(player1);
        harness.setGraveyard(player1, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(weaver), 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(weaver);
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(weaver.getCard().getId()));
    }

    private Permanent addReadyNyxWeaver(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new NyxWeaver());
        perm.setSummoningSick(false);
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
