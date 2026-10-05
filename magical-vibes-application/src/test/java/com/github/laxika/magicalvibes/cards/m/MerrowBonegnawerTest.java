package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LingeringTormentor;
import com.github.laxika.magicalvibes.cards.l.LoyalGyrfalcon;
import com.github.laxika.magicalvibes.cards.o.OdiousTrow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerrowBonegnawer.class, LingeringTormentor.class, LoyalGyrfalcon.class, OdiousTrow.class})
class MerrowBonegnawerTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: target player exiles a chosen card from their graveyard")
    void targetPlayerExilesChosenCard() {
        Permanent bonegnawer = addCreatureReady(player1, new MerrowBonegnawer());
        harness.setGraveyard(player2, List.of(new LoyalGyrfalcon(), new LingeringTormentor()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        assertThat(bonegnawer.isTapped()).isTrue();
        harness.passBothPriorities();

        // Target player chooses which card to exile — pick the creature (index 0)
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()).getFirst().getName())
                .isEqualTo("Lingering Tormentor");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Loyal Gyrfalcon"));
    }

    @Test
    @DisplayName("{T}: the controller may be the targeted player")
    void controllerCanBeTargeted() {
        addCreatureReady(player1, new MerrowBonegnawer());
        harness.setGraveyard(player1, List.of(new LingeringTormentor()));

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lingering Tormentor"));
    }

    @Test
    @DisplayName("{T}: targeting a player with an empty graveyard does nothing")
    void emptyTargetGraveyardDoesNothing() {
        addCreatureReady(player1, new MerrowBonegnawer());
        harness.setGraveyard(player2, List.of());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("{T} ability auto-exiles when the target graveyard has a single card")
    void autoExilesSingleCard() {
        addCreatureReady(player1, new MerrowBonegnawer());
        harness.setGraveyard(player2, List.of(new LingeringTormentor()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lingering Tormentor"));
    }

    @Test
    @DisplayName("Casting a black spell lets the controller untap Merrow Bonegnawer")
    void untapsWhenCastingBlackSpell() {
        Permanent bonegnawer = addCreatureReady(player1, new MerrowBonegnawer());
        bonegnawer.tap();
        harness.castFromHand(player1, new LingeringTormentor(), "{3}{B}");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bonegnawer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a multicolored black spell lets the controller untap Merrow Bonegnawer")
    void untapsWhenCastingMulticoloredBlackSpell() {
        Permanent bonegnawer = addCreatureReady(player1, new MerrowBonegnawer());
        bonegnawer.tap();
        harness.setHand(player1, List.of(new OdiousTrow()));
        harness.addMana(player1, ManaColor.BLACK, 1);

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
        harness.castFromHand(player1, new LingeringTormentor(), "{3}{B}");
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(bonegnawer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A black-green hybrid spell paid with green mana still triggers the untap")
    void untapsWhenHybridSpellIsPaidWithGreenMana() {
        Permanent bonegnawer = addCreatureReady(player1, new MerrowBonegnawer());
        bonegnawer.tap();
        harness.setHand(player1, List.of(new OdiousTrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bonegnawer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent casting a black spell does not trigger the untap")
    void opponentsBlackSpellDoesNotTrigger() {
        Permanent bonegnawer = addCreatureReady(player1, new MerrowBonegnawer());
        bonegnawer.tap();
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new LingeringTormentor(), "{3}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(bonegnawer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-black spell does not trigger the untap")
    void nonBlackSpellDoesNotTrigger() {
        Permanent bonegnawer = addCreatureReady(player1, new MerrowBonegnawer());
        bonegnawer.tap();
        harness.castFromHand(player1, new LoyalGyrfalcon(), "{3}{W}");

        assertThat(gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(bonegnawer.isTapped()).isTrue();
    }
}
