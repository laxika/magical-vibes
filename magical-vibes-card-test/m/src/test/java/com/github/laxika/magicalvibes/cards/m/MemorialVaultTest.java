package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemorialVault.class, SolRing.class, Forest.class, GrizzlyBears.class})
class MemorialVaultTest extends BaseCardTest {

    @Test
    void exilesOnePlusSacrificedArtifactsManaValueAndGrantsPlayPermission() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new MemorialVault());
        Permanent sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(vault.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificedArtifact.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId())
                .doesNotContainKey(third.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(first.getId(), second.getId());
    }

    @Test
    void cannotSacrificeMemorialVaultItself() {
        harness.addToBattlefieldAndReturn(player1, new MemorialVault());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void canPlayExiledLandAndCastExiledSpellByPayingItsCost() {
        harness.addToBattlefield(player1, new MemorialVault());
        harness.addToBattlefield(player1, new SolRing());
        Card land = new Forest();
        Card spell = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, spell));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void chosenArtifactManaValueIsUsedWhenSeveralArtifactsAreAvailable() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MemorialVault());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new MemorialVault());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MemorialVault());
        List<Card> library = List.of(new MemorialVault(), new MemorialVault(), new MemorialVault(),
                new MemorialVault(), new MemorialVault(), new MemorialVault());
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source, other).doesNotContain(chosen);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(library.subList(0, 5));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(5));
    }

    @Test
    void exilesOnlyAvailableCardsWhenLibraryIsShorterThanTheAmount() {
        harness.addToBattlefield(player1, new MemorialVault());
        harness.addToBattlefield(player1, new MemorialVault());
        Card top = new MemorialVault();
        harness.setLibrary(player1, List.of(top));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
    }

    @Test
    void cannotSacrificeAnOpponentsArtifact() {
        harness.addToBattlefield(player1, new MemorialVault());
        harness.addToBattlefield(player2, new MemorialVault());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
