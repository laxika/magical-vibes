package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KolaghanStormsinger;
import com.github.laxika.magicalvibes.cards.f.FoulTongueShriek;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({HedonistsTrove.class, Forest.class, KolaghanStormsinger.class, FoulTongueShriek.class, TormentingVoice.class})
class HedonistsTroveTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles and tracks the targeted opponent's graveyard")
    void exilesTargetOpponentsGraveyardWithTrove() {
        Forest land = new Forest();
        KolaghanStormsinger creature = new KolaghanStormsinger();
        Permanent trove = castTrove(List.of(land, creature));

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(trove.getId()))
                .containsExactly(land, creature);
    }

    @Test
    @DisplayName("Controller may play one tracked land and cast one tracked spell each turn")
    void playsLandAndCastsOneSpellFromTrove() {
        Forest land = new Forest();
        KolaghanStormsinger firstSpell = new KolaghanStormsinger();
        FoulTongueShriek secondSpell = new FoulTongueShriek();
        Permanent trove = castTrove(List.of(land, firstSpell, secondSpell));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Forest");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, firstSpell.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kolaghan Stormsinger");

        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, secondSpell.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(gd.getCardsExiledByPermanent(trove.getId())).contains(secondSpell);
    }

    @Test
    @DisplayName("A tracked land can still be played after the turn's tracked spell")
    void playsLandAfterCastingSpellFromTrove() {
        Forest land = new Forest();
        KolaghanStormsinger spell = new KolaghanStormsinger();
        castTrove(List.of(land, spell));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Kolaghan Stormsinger");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Trove cannot target its controller")
    void cannotTargetController() {
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new HedonistsTrove()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void requiresTheSpellsNormalColoredManaAndFailedCastDoesNotUsePermission() {
        KolaghanStormsinger spell = new KolaghanStormsinger();
        Permanent trove = castTrove(List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(trove.getId())).contains(spell);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kolaghan Stormsinger");
    }

    @Test
    void doesNotGrantTheOwnerPermissionToCastExiledCards() {
        KolaghanStormsinger spell = new KolaghanStormsinger();
        Permanent trove = castTrove(List.of(spell));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player2, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(gd.getCardsExiledByPermanent(trove.getId())).contains(spell);
    }

    @Test
    void allowsAnInstantDuringTheOpponentsTurn() {
        FoulTongueShriek spell = new FoulTongueShriek();
        castTrove(List.of(spell));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
    }

    @Test
    void doesNotGrantSorcerySpeedSpellsFlash() {
        KolaghanStormsinger spell = new KolaghanStormsinger();
        Permanent trove = castTrove(List.of(spell));
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.getCardsExiledByPermanent(trove.getId())).contains(spell);
    }

    @Test
    void doesNotGrantAdditionalLandPlays() {
        Forest first = new Forest();
        Forest second = new Forest();
        Permanent trove = castTrove(List.of(first, second));
        harness.castFromExile(player1, first.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getCardsExiledByPermanent(trove.getId())).contains(second);
    }

    @Test
    void losesPlayPermissionWhenTroveLeavesTheBattlefield() {
        KolaghanStormsinger spell = new KolaghanStormsinger();
        Permanent trove = castTrove(List.of(spell));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, trove));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    void canInitiateCastingASpellWithAPayableAdditionalDiscardCost() {
        TormentingVoice spell = new TormentingVoice();
        castTrove(List.of(spell));
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatCode(() -> harness.castFromExile(player1, spell.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void exilesOnlyTheTargetedOpponentsGraveyard() {
        Forest ownCard = new Forest();
        Forest opponentCard = new Forest();
        harness.setGraveyard(player1, List.of(ownCard));

        Permanent trove = castTrove(List.of(opponentCard));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(trove.getId())).containsExactly(opponentCard);
    }

    @Test
    void resolvesWithAnEmptyOpponentsGraveyard() {
        Permanent trove = castTrove(List.of());

        assertThat(gd.getCardsExiledByPermanent(trove.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Hedonist's Trove");
    }

    private Permanent castTrove(List<Card> graveyard) {
        harness.setGraveyard(player2, graveyard);
        harness.setHand(player1, List.of(new HedonistsTrove()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        UUID troveId = harness.getPermanentId(player1, "Hedonist's Trove");
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(troveId))
                .findFirst()
                .orElseThrow();
    }
}
