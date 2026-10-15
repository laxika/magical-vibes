package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TocasiasDigSite.class, ArgothianSprite.class})
class TocasiasDigSiteTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds one colorless mana")
    void tapAddsColorlessMana() {
        Permanent site = addSiteReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(site.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying three mana and tapping surveils 1")
    void surveilAccepted() {
        Permanent site = addSiteReady(player1);
        Card topCard = new ArgothianSprite();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore + 1);
        harness.assertInGraveyard(player1, "Argothian Sprite");
        assertThat(site.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining surveil 1 leaves the top card on the library")
    void surveilDeclined() {
        Permanent site = addSiteReady(player1);
        Card topCard = new ArgothianSprite();
        gd.playerDecks.get(player1.getId()).add(0, topCard);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(site.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Surveil costs three mana immediately and uses the stack")
    void surveilPaysCostsBeforeResolution() {
        Permanent site = addSiteReady(player1);
        Card topCard = new ArgothianSprite();
        Card secondCard = new ArgothianSprite();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(site.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Surveil with an empty library resolves without a choice")
    void surveilEmptyLibrary() {
        Permanent site = addSiteReady(player1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(site.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
    }

    @Test
    @DisplayName("Two mana cannot pay the surveil activation cost")
    void surveilRequiresThreeMana() {
        Permanent site = addSiteReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(site.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("A newly entered noncreature land can activate its tap ability")
    void newlyEnteredLandCanSurveil() {
        Permanent site = harness.addToBattlefieldAndReturn(player1, new TocasiasDigSite());
        Card topCard = new ArgothianSprite();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(site.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addSiteReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TocasiasDigSite());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
