package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkDwellerOracle.class, GrizzlyBears.class, Mountain.class, TormentingVoice.class})
class DarkDwellerOracleTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and exiles the top card with end-of-turn play permission")
    void sacrificesCreatureAndExilesTopCard() {
        addOracleAndFodder();
        Card top = putSpellOnTop(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID fodderId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodderId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    @DisplayName("Can sacrifice itself to activate")
    void canSacrificeItself() {
        harness.addToBattlefield(player1, new DarkDwellerOracle());
        Card top = putSpellOnTop(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dark-Dweller Oracle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(top.getId()));
    }

    @Test
    @DisplayName("Exiles nothing when the library is empty")
    void exilesNothingWithEmptyLibrary() {
        addOracleAndFodder();
        gd.playerDecks.get(player1.getId()).clear();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID fodderId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodderId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions).isEmpty();
    }


    @Test
    void paysSacrificeBeforeResolutionAndUsesCurrentLibraryTop() {
        var oracle = harness.addToBattlefieldAndReturn(player1, new DarkDwellerOracle());
        oracle.setSummoningSick(true);
        oracle.tap();
        Card originalTop = new Mountain();
        Card resolutionTop = new DarkDwellerOracle();
        harness.setLibrary(player1, List.of(originalTop));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Dark-Dweller Oracle");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.setLibrary(player1, List.of(resolutionTop, originalTop));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(resolutionTop);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
    }

    @Test
    void exiledCreatureRequiresNormalManaAndCanOnlyBeCastOnce() {
        Card top = new DarkDwellerOracle();
        exileBySacrificingOracle(top);
        gd.playerManaPools.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, top.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exiledCreatureCannotBeCastDuringCombat() {
        Card top = new DarkDwellerOracle();
        exileBySacrificingOracle(top);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void exiledLandUsesAnAvailableLandPlay() {
        Card top = new Mountain();
        exileBySacrificingOracle(top);

        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    @Test
    void exiledLandCannotBePlayedAfterUsingLandPlay() {
        Card top = new Mountain();
        exileBySacrificingOracle(top);
        harness.setHand(player1, List.of(new Mountain()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void exiledLandCannotBePlayedDuringCombat() {
        Card top = new Mountain();
        exileBySacrificingOracle(top);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void unusedCardRemainsExiledButPermissionExpiresDuringCleanup() {
        Card top = new DarkDwellerOracle();
        exileBySacrificingOracle(top);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, top.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void canBeginCastingExiledSpellWithPayableMandatoryAdditionalCost() {
        Card top = new TormentingVoice();
        exileBySacrificingOracle(top);
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatCode(() -> harness.castFromExile(player1, top.getId()))
                .doesNotThrowAnyException();
    }

    private void exileBySacrificingOracle(Card top) {
        harness.addToBattlefield(player1, new DarkDwellerOracle());
        harness.setLibrary(player1, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void addOracleAndFodder() {
        harness.addToBattlefield(player1, new DarkDwellerOracle());
        harness.addToBattlefield(player1, new GrizzlyBears());
    }

    private Card putSpellOnTop(Player player) {
        Card card = new Card();
        card.setName("Exiled Spell");
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}{R}");
        card.setColor(CardColor.RED);
        gd.playerDecks.get(player.getId()).addFirst(card);
        return card;
    }
}
