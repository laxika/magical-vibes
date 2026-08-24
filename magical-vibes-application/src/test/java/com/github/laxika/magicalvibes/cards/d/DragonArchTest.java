package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonArch.class, AdelizTheCinderWind.class, Forest.class, GrizzlyBears.class})
class DragonArchTest extends BaseCardTest {

    @Test
    @DisplayName("The ability offers only multicolored creature cards from hand")
    void abilityOffersOnlyMulticoloredCreatures() {
        addReadyDragonArch();
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(2);
    }

    @Test
    @DisplayName("Choosing a multicolored creature puts it onto the battlefield untapped")
    void choosingMulticoloredCreaturePutsItOntoBattlefield() {
        Permanent arch = addReadyDragonArch();
        AdelizTheCinderWind creature = new AdelizTheCinderWind();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Adeliz, the Cinder Wind");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(arch.isTapped()).isTrue();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .allMatch(permanent -> !permanent.isTapped())).isTrue();
    }

    @Test
    @DisplayName("Declining the may choice leaves the hand unchanged")
    void decliningMayLeavesHandUnchanged() {
        addReadyDragonArch();
        List<Card> hand =
                List.of(new AdelizTheCinderWind(), new GrizzlyBears(), new Forest());
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactlyElementsOf(hand);
        harness.assertNotOnBattlefield(player1, "Adeliz, the Cinder Wind");
    }

    private Permanent addReadyDragonArch() {
        return harness.addToBattlefieldAndReturn(player1, new DragonArch());
    }
}
