package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BaronyVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MerrowBonegnawerTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: target player exiles a chosen card from their graveyard")
    void targetPlayerExilesChosenCard() {
        addCreatureReady(player1, new MerrowBonegnawer());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        // Target player chooses which card to exile — pick the creature (index 0)
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()).getFirst().getName()).isEqualTo("Shock");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("{T} ability auto-exiles when the target graveyard has a single card")
    void autoExilesSingleCard() {
        addCreatureReady(player1, new MerrowBonegnawer());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Casting a black spell lets the controller untap Merrow Bonegnawer")
    void untapsWhenCastingBlackSpell() {
        Permanent bonegnawer = addCreatureReady(player1, new MerrowBonegnawer());
        bonegnawer.tap();
        harness.setHand(player1, List.of(new BaronyVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bonegnawer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the may ability leaves Merrow Bonegnawer tapped")
    void staysTappedWhenDeclining() {
        Permanent bonegnawer = addCreatureReady(player1, new MerrowBonegnawer());
        bonegnawer.tap();
        harness.setHand(player1, List.of(new BaronyVampire()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(bonegnawer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-black spell does not trigger the untap")
    void nonBlackSpellDoesNotTrigger() {
        Permanent bonegnawer = addCreatureReady(player1, new MerrowBonegnawer());
        bonegnawer.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(bonegnawer.isTapped()).isTrue();
    }
}
