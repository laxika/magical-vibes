package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

// Note: Mistveil Plains is colorless (CR 202.2 — a land has no mana cost), so it does not count
// itself toward its own "two or more white permanents" activation restriction.
class MistveilPlainsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts target graveyard card on the bottom of the library with two or more white permanents")
    void tucksTargetToBottomOfLibrary() {
        Permanent plains = addPlains(player1);
        addCreatureReady(player1, new EliteVanguard());
        addCreatureReady(player1, new SuntailHawk()); // the Plains is colorless, so these two are the pair
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant(), new GrizzlyBears())));

        int plainsIdx = gd.playerBattlefields.get(player1.getId()).indexOf(plains);
        harness.activateAbilityWithGraveyardTargets(player1, plainsIdx, 1, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.get(library.size() - 1).getId()).isEqualTo(tucked.getId());
    }

    @Test
    @DisplayName("Cannot activate with fewer than two white permanents")
    void rejectedWithTooFewWhitePermanents() {
        Permanent plains = addPlains(player1);
        // One white permanent — the colorless Plains does not make up the second.
        addCreatureReady(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));

        int plainsIdx = gd.playerBattlefields.get(player1.getId()).indexOf(plains);
        UUID tuckedId = tucked.getId();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, plainsIdx, 1, List.of(tuckedId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-white permanents do not count toward the activation restriction")
    void nonWhitePermanentsDoNotCount() {
        Permanent plains = addPlains(player1);
        addCreatureReady(player1, new GrizzlyBears()); // green — does not count
        harness.addMana(player1, ManaColor.WHITE, 1);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));

        int plainsIdx = gd.playerBattlefields.get(player1.getId()).indexOf(plains);
        UUID tuckedId = tucked.getId();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, plainsIdx, 1, List.of(tuckedId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability adds white mana")
    void manaAbilityAddsWhite() {
        Permanent plains = addPlains(player1);

        int plainsIdx = gd.playerBattlefields.get(player1.getId()).indexOf(plains);
        harness.activateAbility(player1, plainsIdx, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    private Permanent addPlains(Player player) {
        harness.addToBattlefield(player, new MistveilPlains());
        Permanent plains = findPermanent(player, "Mistveil Plains");
        plains.setSummoningSick(false);
        plains.untap();
        return plains;
    }
}
