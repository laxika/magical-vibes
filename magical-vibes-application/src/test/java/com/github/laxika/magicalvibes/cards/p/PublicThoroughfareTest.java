package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PublicThoroughfare.class, Forest.class, Spellbook.class})
class PublicThoroughfareTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        playPublicThoroughfare();

        Permanent thoroughfare = findThoroughfare(player1);
        assertThat(thoroughfare).isNotNull();
        assertThat(thoroughfare.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping an untapped land keeps Public Thoroughfare on the battlefield")
    void tappingLandKeepsIt() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        playPublicThoroughfare();
        resolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(forest.isTapped()).isTrue();
        assertThat(findThoroughfare(player1)).isNotNull();
        harness.assertNotInGraveyard(player1, "Public Thoroughfare");
    }

    @Test
    @DisplayName("Tapping an untapped artifact keeps Public Thoroughfare on the battlefield")
    void tappingArtifactKeepsIt() {
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        playPublicThoroughfare();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(spellbook.isTapped()).isTrue();
        assertThat(findThoroughfare(player1)).isNotNull();
    }

    @Test
    @DisplayName("Declining to tap an artifact or land sacrifices Public Thoroughfare")
    void decliningTapSacrificesIt() {
        playPublicThoroughfare();
        resolveEnterTrigger();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findThoroughfare(player1)).isNull();
        harness.assertInGraveyard(player1, "Public Thoroughfare");
    }

    @Test
    @DisplayName("Tap ability adds one mana of the chosen color")
    void tapAddsChosenColorMana() {
        Permanent thoroughfare = addThoroughfareReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(thoroughfare.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void playPublicThoroughfare() {
        harness.setHand(player1, List.of(new PublicThoroughfare()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private void resolveEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addThoroughfareReady(Player player) {
        Permanent thoroughfare = new Permanent(new PublicThoroughfare());
        thoroughfare.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(thoroughfare);
        return thoroughfare;
    }

    private Permanent findThoroughfare(Player player) {
        return findPermanents(player, "Public Thoroughfare").stream().findFirst().orElse(null);
    }
}
