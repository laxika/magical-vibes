package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Chronomancer.class, Spellbook.class, GrizzlyBears.class})
class ChronomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another artifact draws a card")
    void atomicTransmutationDrawsCard() {
        addReadyChronomancer(player1);
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Cannot sacrifice Chronomancer itself")
    void cannotSacrificeSource() {
        addReadyChronomancer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: another artifact");
    }

    @Test
    @DisplayName("Unearth returns Chronomancer with haste")
    void unearthReturnsWithHaste() {
        harness.setGraveyard(player1, List.of(new Chronomancer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent chronomancer = findPermanent(player1, "Chronomancer");
        assertThat(chronomancer.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Chronomancer");
    }

    @Test
    @DisplayName("Unearthed Chronomancer is exiled at the next end step")
    void unearthExilesAtNextEndStep() {
        harness.setGraveyard(player1, List.of(new Chronomancer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Chronomancer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Chronomancer"));
    }

    private Permanent addReadyChronomancer(com.github.laxika.magicalvibes.model.Player player) {
        Permanent chronomancer = harness.addToBattlefieldAndReturn(player, new Chronomancer());
        chronomancer.setSummoningSick(false);
        return chronomancer;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
