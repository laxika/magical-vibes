package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChulaneTellerOfTales.class, Forest.class, GrizzlyBears.class})
class ChulaneTellerOfTalesTest extends BaseCardTest {

    @Test
    void creatureSpellDrawsAndMayPutLandFromHandOntoBattlefield() {
        harness.addToBattlefield(player1, new ChulaneTellerOfTales());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .extracting(card -> card.getName())
                .contains("Chulane, Teller of Tales", "Forest", "Grizzly Bears");
    }

    @Test
    void decliningLandEntryStillDraws() {
        harness.addToBattlefield(player1, new ChulaneTellerOfTales());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest", "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .extracting(card -> card.getName())
                .contains("Chulane, Teller of Tales", "Grizzly Bears")
                .doesNotContain("Forest");
    }

    @Test
    void activatedAbilityReturnsTargetCreatureYouControl() {
        Permanent chulane = harness.addToBattlefieldAndReturn(player1, new ChulaneTellerOfTales());
        chulane.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(chulane.isTapped()).isTrue();
    }

    @Test
    void activatedAbilityCannotTargetOpponentCreature() {
        Permanent chulane = harness.addToBattlefieldAndReturn(player1, new ChulaneTellerOfTales());
        chulane.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawnLandCanEnterBeforeCreatureSpellResolves() {
        harness.addToBattlefield(player1, new ChulaneTellerOfTales());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void creatureEnteringWithoutBeingCastDoesNotDraw() {
        harness.addToBattlefield(player1, new ChulaneTellerOfTales());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void activatedAbilityCanReturnChulaneItself() {
        Permanent chulane = harness.addToBattlefieldAndReturn(player1, new ChulaneTellerOfTales());
        chulane.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, chulane.getId());
        assertThat(chulane.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Chulane, Teller of Tales");
        harness.assertNotOnBattlefield(player1, "Chulane, Teller of Tales");
    }

    @Test
    void summoningSickChulaneCannotActivateTapAbility() {
        harness.addToBattlefield(player1, new ChulaneTellerOfTales());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void drawingNonlandStillWorksWhenNoLandIsAvailable() {
        harness.addToBattlefield(player1, new ChulaneTellerOfTales());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void opponentsCreatureSpellDoesNotTriggerChulane() {
        harness.addToBattlefield(player1, new ChulaneTellerOfTales());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
