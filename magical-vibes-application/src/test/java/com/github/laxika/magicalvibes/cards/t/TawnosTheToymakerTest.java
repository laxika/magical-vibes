package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoastalBulwark;
import com.github.laxika.magicalvibes.cards.d.DreamtailHeron;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TawnosTheToymaker.class, ObstinateBaloth.class, DreamtailHeron.class, CoastalBulwark.class})
class TawnosTheToymakerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting a Beast spell creates an artifact token copy")
    void beastSpellCreatesArtifactTokenCopy() {
        harness.addToBattlefield(player1, new TawnosTheToymaker());
        harness.castFromHand(player1, new ObstinateBaloth(), "{2}{G}{G}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        List<Permanent> beasts = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Obstinate Baloth"))
                .toList();
        assertThat(beasts).hasSize(2);
        assertThat(beasts).anySatisfy(permanent -> {
            assertThat(permanent.getCard().isToken()).isTrue();
            assertThat(permanent.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(gqs.isArtifact(gd, permanent)).isTrue();
        });
        assertThat(beasts).anySatisfy(permanent -> {
            assertThat(permanent.getCard().isToken()).isFalse();
            assertThat(gqs.isArtifact(gd, permanent)).isFalse();
        });
    }

    @Test
    @DisplayName("A Bird spell also triggers Tawnos")
    void birdSpellAlsoTriggers() {
        harness.addToBattlefield(player1, new TawnosTheToymaker());
        harness.castFromHand(player1, new DreamtailHeron(), "{4}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Dreamtail Heron"))
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the copy leaves only the original creature")
    void declineCopy() {
        harness.addToBattlefield(player1, new TawnosTheToymaker());
        harness.castFromHand(player1, new ObstinateBaloth(), "{2}{G}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Obstinate Baloth")))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    @DisplayName("A non-Beast, non-Bird creature does not trigger Tawnos")
    void otherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new TawnosTheToymaker());
        harness.castFromHand(player1, new CoastalBulwark(), "{2}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Coastal Bulwark")))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    @DisplayName("Both the original Beast and its copy trigger their enter abilities")
    void copyRetainsEnterAbilityWithoutTriggeringAnotherSpellCopy() {
        harness.addToBattlefield(player1, new TawnosTheToymaker());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new ObstinateBaloth(), "{2}{G}{G}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 28);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Obstinate Baloth")))
                .hasSize(2);
    }

    @Test
    @DisplayName("An opponent's Beast spell does not trigger Tawnos")
    void opponentBeastDoesNotTrigger() {
        harness.addToBattlefield(player1, new TawnosTheToymaker());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ObstinateBaloth(), "{2}{G}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Obstinate Baloth")))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    @DisplayName("The copy ability resolves even after Tawnos leaves the battlefield")
    void copyAbilitySurvivesSourceLeaving() {
        Permanent tawnos = harness.addToBattlefieldAndReturn(player1, new TawnosTheToymaker());
        harness.castFromHand(player1, new ObstinateBaloth(), "{2}{G}{G}");
        gd.playerBattlefields.get(player1.getId()).remove(tawnos);
        gd.playerGraveyards.get(player1.getId()).add(tawnos.getCard());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Obstinate Baloth")))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Tawnos, the Toymaker");
    }

}
