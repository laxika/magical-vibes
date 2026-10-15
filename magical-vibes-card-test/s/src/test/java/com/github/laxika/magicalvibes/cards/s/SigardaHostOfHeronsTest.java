package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.a.AlchemistsApprentice;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.d.DemonicTaskmaster;
import com.github.laxika.magicalvibes.cards.f.FadeAway;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KillingWave;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigardaHostOfHerons.class, BarterInBlood.class, CruelEdict.class, GrizzlyBears.class,
        AlchemistsApprentice.class, DemonicTaskmaster.class, KillingWave.class, FadeAway.class})
class SigardaHostOfHeronsTest extends BaseCardTest {

    private long creatureCount(Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count();
    }

    private void castBarterInBlood() {
        harness.castFromHand(player1, new BarterInBlood(), "{2}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's targeted edict can't make Sigarda's controller sacrifice")
    void opponentTargetedEdictDoesNothing() {
        harness.addToBattlefield(player2, new SigardaHostOfHerons());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Sigarda, Host of Herons");
    }

    @Test
    @DisplayName("An opponent's each-player edict skips Sigarda's controller but still hits the caster")
    void opponentEachPlayerEdictSkipsProtectedPlayer() {
        harness.addToBattlefield(player2, new SigardaHostOfHerons());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castBarterInBlood();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(creatureCount(player1)).isZero();
        // Sigarda plus both Bears are untouched
        assertThat(creatureCount(player2)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sigarda doesn't stop her controller's own sacrifice effects")
    void ownEffectStillCausesSacrifice() {
        harness.addToBattlefield(player1, new SigardaHostOfHerons());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castBarterInBlood();

        // Player1 has three creatures and must choose two of them to sacrifice
        assertThat(harness.getGameData().interaction.activeInteraction()).isNotNull();
    }

    @Test
    @DisplayName("Protection is lost once Sigarda leaves the battlefield")
    void protectionEndsWhenSigardaLeaves() {
        Permanent sigarda = harness.addToBattlefieldAndReturn(player2, new SigardaHostOfHerons());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.getGameData().playerBattlefields.get(player2.getId()).remove(sigarda);

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining an opponent's Killing Wave payment sacrifices no protected creatures")
    void opponentKillingWaveCannotSacrificeProtectedCreatures() {
        harness.addToBattlefield(player2, new SigardaHostOfHerons());
        harness.addToBattlefield(player2, new AlchemistsApprentice());
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultiplePermanentsChosen(player2, List.of());

        harness.assertOnBattlefield(player2, "Sigarda, Host of Herons");
        harness.assertOnBattlefield(player2, "Alchemist's Apprentice");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sigarda still protects creatures when Killing Wave life payments are unaffordable")
    void unaffordableOpponentKillingWaveCannotCauseSacrifice() {
        harness.addToBattlefield(player2, new SigardaHostOfHerons());
        harness.addToBattlefield(player2, new AlchemistsApprentice());
        harness.setLife(player2, 1);
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertOnBattlefield(player2, "Sigarda, Host of Herons");
        harness.assertOnBattlefield(player2, "Alchemist's Apprentice");
        harness.assertLife(player2, 1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Sigarda's controller may pay Killing Wave life voluntarily without sacrificing other creatures")
    void protectedPlayerCanPayForOnlyOneCreature() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player2, new AlchemistsApprentice());
        harness.addToBattlefield(player2, new SigardaHostOfHerons());
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultiplePermanentsChosen(player2, List.of(apprentice.getId()));

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Sigarda, Host of Herons");
        harness.assertOnBattlefield(player2, "Alchemist's Apprentice");
    }

    @Test
    @DisplayName("Sigarda does not prevent her controller from paying a sacrifice activation cost")
    void ownSacrificeCostStillWorks() {
        harness.addToBattlefield(player1, new SigardaHostOfHerons());
        harness.addToBattlefield(player1, new AlchemistsApprentice());
        AlchemistsApprentice drawnCard = new AlchemistsApprentice();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 1, null, null);

        harness.assertInGraveyard(player1, "Alchemist's Apprentice");
        harness.assertNotOnBattlefield(player1, "Alchemist's Apprentice");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertOnBattlefield(player1, "Sigarda, Host of Herons");
    }

    @Test
    @DisplayName("Sigarda may be sacrificed to her controller's own upkeep trigger")
    void ownTriggeredAbilityCanSacrificeSigarda() {
        harness.addToBattlefield(player1, new DemonicTaskmaster());
        harness.addToBattlefield(player1, new SigardaHostOfHerons());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sigarda, Host of Herons");
        harness.assertNotOnBattlefield(player1, "Sigarda, Host of Herons");
        harness.assertOnBattlefield(player1, "Demonic Taskmaster");
    }

    @Test
    @DisplayName("Sigarda's controller may voluntarily pay mana for an opponent's Fade Away")
    void protectedPlayerCanChooseToPayFadeAway() {
        Permanent sigarda = harness.addToBattlefieldAndReturn(player2, new SigardaHostOfHerons());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new FadeAway(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(sigarda.getId()));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player2, "Sigarda, Host of Herons");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
