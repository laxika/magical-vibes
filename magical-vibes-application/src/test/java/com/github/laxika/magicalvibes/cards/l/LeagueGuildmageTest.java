package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeagueGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("Draw ability draws a card")
    void drawAbilityDrawsCard() {
        addReadyGuildmage(player1);
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Copy ability copies an instant or sorcery spell with matching mana value")
    void copyAbilityCopiesSpellWithMatchingManaValue() {
        addReadyGuildmage(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.activateAbility(player1, 0, 1, 3, counsel.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .allMatch(copy -> copy.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Copy ability requires the target spell's mana value to equal X")
    void copyAbilityRequiresMatchingX() {
        addReadyGuildmage(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 1, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copy ability cannot target a creature spell")
    void copyAbilityCannotTargetCreatureSpell() {
        addReadyGuildmage(player1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copy ability cannot target an opponent's spell")
    void copyAbilityCannotTargetOpponentSpell() {
        addReadyGuildmage(player1);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);

        UUID counselId = counsel.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, counselId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyGuildmage(Player player) {
        Permanent permanent = new Permanent(new LeagueGuildmage());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
