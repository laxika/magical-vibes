package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MirrorGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability exiles and imprints a card from a graveyard")
    void acceptsGraveyardImprint() {
        Card imprinted = new RodOfRuin();

        Permanent golem = castMirrorGolem(imprinted, true);

        harness.assertNotInGraveyard(player2, "Rod of Ruin");
        assertThat(gd.getImprintedCard(golem.getCard())).isSameAs(imprinted);
        assertThat(gd.getCardsExiledByPermanent(golem.getId())).containsExactly(imprinted);
    }

    @Test
    @DisplayName("Gains protection from the imprinted card's card types")
    void protectsFromImprintedCardTypes() {
        Permanent golem = castMirrorGolem(new RodOfRuin(), true);
        Permanent rod = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        rod.setSummoningSick(false);
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, golem, rod)).isTrue();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        int rodIndex = gd.playerBattlefields.get(player2.getId()).indexOf(rod);
        assertThatThrownBy(() -> harness.activateAbility(player2, rodIndex, null, golem.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Declining the ETB ability grants no protection")
    void declinesGraveyardImprint() {
        Permanent golem = castMirrorGolem(new RodOfRuin(), false);
        Permanent rod = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        rod.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        int rodIndex = gd.playerBattlefields.get(player2.getId()).indexOf(rod);
        harness.activateAbility(player2, rodIndex, null, golem.getId());
        harness.passBothPriorities();

        assertThat(golem.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent castMirrorGolem(Card graveyardCard, boolean accept) {
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCard)));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MirrorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);

        return findPermanent(player1, "Mirror Golem");
    }
}
