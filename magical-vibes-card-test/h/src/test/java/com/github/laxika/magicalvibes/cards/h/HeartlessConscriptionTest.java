package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiselaTheBrokenBlade;
import com.github.laxika.magicalvibes.cards.b.BrunaTheFadingLight;
import com.github.laxika.magicalvibes.cards.b.BriselaVoiceOfNightmares;
import com.github.laxika.magicalvibes.cards.m.MirrorGallery;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({HeartlessConscription.class, GrizzlyBears.class, MirrorGallery.class, HumbleDefector.class,
        GiselaTheBrokenBlade.class, BrunaTheFadingLight.class, BriselaVoiceOfNightmares.class})
class HeartlessConscriptionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all creatures, leaves noncreatures, and exiles itself")
    void exilesCreaturesAndItself() {
        harness.addToBattlefield(player1, new MirrorGallery());
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        HeartlessConscription spell = new HeartlessConscription();
        harness.castFromHand(player1, spell, "{6}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Mirror Gallery");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(player1Creature.getCard())
                .doesNotContain(player2Creature.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(player2Creature.getCard());
    }

    @Test
    @DisplayName("Lets its controller cast an opponent's exiled creature with any mana")
    void controllerCanCastOpponentCreatureWithAnyMana() {
        Card creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getCard();
        HeartlessConscription spell = new HeartlessConscription();
        harness.castFromHand(player1, spell, "{6}{B}{B}");
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    @DisplayName("Resolves and exiles itself with no creatures on the battlefield")
    void resolvesWithoutCreatures() {
        HeartlessConscription spell = new HeartlessConscription();
        harness.castFromHand(player1, spell, "{6}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        harness.assertNotInGraveyard(player1, "Heartless Conscription");
    }

    @Test
    @DisplayName("The exiled spell itself does not receive play permission")
    void cannotRecastConscriptionItself() {
        HeartlessConscription spell = new HeartlessConscription();
        harness.castFromHand(player1, spell, "{6}{B}{B}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 8);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("A creature's owner cannot use the controller's play permission")
    void creatureOwnerDoesNotReceivePermission() {
        Card creature = harness.addToBattlefieldAndReturn(player2, new HumbleDefector()).getCard();
        harness.castFromHand(player1, new HeartlessConscription(), "{6}{B}{B}");
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Playing exiled creatures still requires normal sorcery timing")
    void permissionDoesNotOverrideTiming() {
        Card creature = harness.addToBattlefieldAndReturn(player2, new HumbleDefector()).getCard();
        harness.castFromHand(player1, new HeartlessConscription(), "{6}{B}{B}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("Any-mana permission does not waive the cost or disappear after failed payment")
    void failedPaymentPreservesPermission() {
        Card creature = harness.addToBattlefieldAndReturn(player2, new HumbleDefector()).getCard();
        harness.castFromHand(player1, new HeartlessConscription(), "{6}{B}{B}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Humble Defector");
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    @DisplayName("The controller can play its own exiled creature with colorless mana on a later turn")
    void permissionAndAnyManaPersistAcrossTurns() {
        Card creature = harness.addToBattlefieldAndReturn(player1, new HumbleDefector()).getCard();
        harness.setLibrary(player1, List.of(new HumbleDefector(), new HumbleDefector()));
        harness.setLibrary(player2, List.of(new HumbleDefector(), new HumbleDefector()));
        harness.castFromHand(player1, new HeartlessConscription(), "{6}{B}{B}");
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Humble Defector");
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    @DisplayName("Exiled creature tokens cease to exist and cannot be played")
    void exiledTokensCannotBePlayed() {
        HumbleDefector token = new HumbleDefector();
        token.setToken(true);
        harness.addToBattlefield(player2, token);
        harness.castFromHand(player1, new HeartlessConscription(), "{6}{B}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Humble Defector");
        assertThat(gd.findExiledCard(token.getId())).isNull();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.castFromExile(player1, token.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not found in exile");
    }

    @Test
    @DisplayName("Grants play permission for both cards exiled from a melded creature")
    void canPlayBothExiledMeldComponents() {
        GiselaTheBrokenBlade gisela = new GiselaTheBrokenBlade();
        BrunaTheFadingLight bruna = new BrunaTheFadingLight();
        harness.addToBattlefield(player2, gisela);
        harness.addToBattlefield(player2, bruna);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Brisela, Voice of Nightmares");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new HeartlessConscription(), "{6}{B}{B}");
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(gisela, bruna);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 11);
        harness.castFromExile(player1, gisela.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, bruna.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gisela, the Broken Blade");
        harness.assertOnBattlefield(player1, "Bruna, the Fading Light");
        assertThat(gd.findExiledCard(gisela.getId())).isNull();
        assertThat(gd.findExiledCard(bruna.getId())).isNull();
    }
}
