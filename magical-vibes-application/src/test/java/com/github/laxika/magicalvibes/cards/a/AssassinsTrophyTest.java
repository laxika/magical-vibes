package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssassinsTrophyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target permanent and prompts its controller to search for a basic land")
    void destroysPermanentAndPresentsSearch() {
        Permanent target = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(target);
        setupLibrary(player2);
        castTrophy(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Its controller can put the chosen basic land onto the battlefield untapped")
    void chosenLandEntersUntapped() {
        Permanent target = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(target);
        setupLibrary(player2);
        castTrophy(target);

        harness.passBothPriorities();
        Set<UUID> battlefieldBefore = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .collect(Collectors.toSet());
        harness.getGameService().handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> !battlefieldBefore.contains(p.getId()) && !p.isTapped());
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        Permanent target = new Permanent(new Forest());
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.setHand(player1, List.of(new AssassinsTrophy()));
        addTrophyMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    private void castTrophy(Permanent target) {
        harness.setHand(player1, List.of(new AssassinsTrophy()));
        addTrophyMana();
        harness.castInstant(player1, 0, target.getId());
    }

    private void addTrophyMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void setupLibrary(Player player) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest()));
    }
}
