package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(kami);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 0, null, shock.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only Arcane cards in your own graveyard are offered as targets")
    void offersOnlyOwnArcaneCards() {
        Permanent kami = addReadyKami();
        Card ownBreath = new BlessedBreath();
        Card opponentBreath = new BlessedBreath();
        harness.setGraveyard(player1, List.of(ownBreath, new Shock()));
        harness.setGraveyard(player2, List.of(opponentBreath));

        ValidTargetsResponse response = harness.getValidTargetService().computeValidTargetsForAbility(
                gd, kami.getCard(), kami.getCard().getActivatedAbilities().getFirst(),
                player1.getId(), gd.playerBattlefields.get(player1.getId()).indexOf(kami));

        assertThat(response.validGraveyardCardIds()).containsExactly(ownBreath.getId());
    }

    private Permanent addReadyKami() {
        Permanent perm = new Permanent(new HanaKami());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void activate(Permanent kami, Card graveyardCard) {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(kami);
        harness.activateAbility(player1, idx, 0, null, graveyardCard.getId(), Zone.GRAVEYARD);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
