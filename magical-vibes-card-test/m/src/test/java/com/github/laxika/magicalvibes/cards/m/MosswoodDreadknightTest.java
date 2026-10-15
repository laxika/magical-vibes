package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DreadWhispers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MosswoodDreadknight.class, DreadWhispers.class, Forest.class})
class MosswoodDreadknightTest extends BaseCardTest {

    @Test
    void adventureDrawsACardAndLosesLife() {
        Forest draw = new Forest();
        MosswoodDreadknight card = new MosswoodDreadknight();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertInHand(player1, "Forest");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void deathMayCastAdventureFromGraveyard() {
        MosswoodDreadknight card = new MosswoodDreadknight();
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureResolutionAllowsCastingCreatureFromExile() {
        MosswoodDreadknight card = new MosswoodDreadknight();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mosswood Dreadknight");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertLife(player1, 19);
    }

    @Test
    void decliningDeathPermissionPreventsCastingAdventureFromGraveyard() {
        MosswoodDreadknight card = new MosswoodDreadknight();
        harness.addToBattlefieldAndReturn(player1, card).setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventureFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Mosswood Dreadknight");
        harness.assertLife(player1, 20);
    }

    @Test
    void graveyardAdventureStillRequiresSorceryTiming() {
        MosswoodDreadknight card = new MosswoodDreadknight();
        harness.addToBattlefieldAndReturn(player1, card).setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventureFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Mosswood Dreadknight");
    }

    @Test
    void deathDuringOwnTurnAllowsAdventureDuringNextOwnTurn() {
        MosswoodDreadknight card = new MosswoodDreadknight();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.addToBattlefieldAndReturn(player1, card).setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventureFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertNotInGraveyard(player1, "Mosswood Dreadknight");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void deathDuringOpponentsTurnAllowsAdventureDuringNextOwnTurn() {
        MosswoodDreadknight card = new MosswoodDreadknight();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefieldAndReturn(player1, card).setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventureFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
