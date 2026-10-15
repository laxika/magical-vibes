package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HanaKami.class, BlessedBreath.class, IsamaruHoundOfKonda.class})
class HanaKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Hana Kami returns an Arcane card from your graveyard to your hand")
    void returnsArcaneCardToHand() {
        Permanent kami = addReadyKami();
        Card breath = new BlessedBreath();
        harness.setGraveyard(player1, List.of(breath));

        activate(kami, breath);

        harness.assertInHand(player1, "Blessed Breath");
        harness.assertNotInGraveyard(player1, "Blessed Breath");
        harness.assertNotOnBattlefield(player1, "Hana Kami");
        harness.assertInGraveyard(player1, "Hana Kami");
    }

    @Test
    @DisplayName("Cannot target a non-Arcane card in the graveyard")
    void cannotTargetNonArcaneCard() {
        Permanent kami = addReadyKami();
        Card nonArcane = new IsamaruHoundOfKonda();
        harness.setGraveyard(player1, List.of(nonArcane));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(kami);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 0, null, nonArcane.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only Arcane cards in your own graveyard are offered as targets")
    void offersOnlyOwnArcaneCards() {
        Permanent kami = addReadyKami();
        Card ownBreath = new BlessedBreath();
        Card opponentBreath = new BlessedBreath();
        harness.setGraveyard(player1, List.of(ownBreath, new IsamaruHoundOfKonda()));
        harness.setGraveyard(player2, List.of(opponentBreath));

        ValidTargetsResponse response = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, kami.getCard(), kami.getCard().getActivatedAbilities().getFirst(),
                player1.getId(), gd.playerBattlefields.get(player1.getId()).indexOf(kami));

        assertThat(response.validGraveyardCardIds()).containsExactly(ownBreath.getId());
    }

    private Permanent addReadyKami() {
        return addCreatureReady(player1, new HanaKami());
    }

    @Test
    @DisplayName("Sacrifice is paid before the ability resolves")
    void sacrificesAsAnActivationCost() {
        Permanent kami = addReadyKami();
        Card breath = new BlessedBreath();
        harness.setGraveyard(player1, List.of(breath));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, breath.getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Hana Kami");
        harness.assertInGraveyard(player1, "Hana Kami");
        harness.assertInGraveyard(player1, "Blessed Breath");
        harness.assertNotInHand(player1, "Blessed Breath");
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertInHand(player1, "Blessed Breath");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Hana Kami can activate during an opponent's turn")
    void activatesWithoutTapOrTimingRestrictions() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new HanaKami());
        kami.setSummoningSick(true);
        kami.tap();
        Card breath = new BlessedBreath();
        harness.setGraveyard(player1, List.of(breath));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 0, null, breath.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        harness.assertInHand(player1, "Blessed Breath");
        harness.assertInGraveyard(player1, "Hana Kami");
    }

    @Test
    @DisplayName("Cannot activate targeting an opponent's Arcane card")
    void rejectsOpponentGraveyardTarget() {
        addReadyKami();
        Card breath = new BlessedBreath();
        harness.setGraveyard(player2, List.of(breath));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, breath.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hana Kami");
        harness.assertInGraveyard(player2, "Blessed Breath");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An absent target is not returned and the sacrifice is not refunded")
    void targetLeavingGraveyardMakesAbilityFizzle() {
        addReadyKami();
        Card breath = new BlessedBreath();
        harness.setGraveyard(player1, List.of(breath));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, 0, null, breath.getId(), Zone.GRAVEYARD);

        gd.playerGraveyards.get(player1.getId()).remove(breath);
        harness.setExile(player1, List.of(breath));
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Blessed Breath");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(breath);
        harness.assertInGraveyard(player1, "Hana Kami");
        harness.assertNotOnBattlefield(player1, "Hana Kami");
        assertThat(gd.stack).isEmpty();
    }

    private void activate(Permanent kami, Card graveyardCard) {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(kami);
        harness.activateAbility(player1, idx, 0, null, graveyardCard.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();
    }
}
