package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoneSplinters.class, GrizzlyBears.class, AirElemental.class})
class BoneSplintersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and destroys the target creature")
    void sacrificesCreatureAndDestroysTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new BoneSplinters()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new BoneSplinters()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Sacrifice is paid before the spell resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BoneSplinters()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Bone Splinters");
    }

    @Test
    @DisplayName("Can destroy another creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new BoneSplinters()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Air Elemental");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature to pay the cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new BoneSplinters()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0,
                opponentCreature.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, GravebaneZombie.class})
    @DisplayName("Sacrificing Gravebane Zombie under an opponent's Kalitas asks library-or-Kalitas before the cost is paid")
    void sacrificeCostAsksTheDeathReplacementBeforePayment() {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card splinters = new BoneSplinters();
        harness.setHand(player1, List.of(splinters));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), gravebane.getId());

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");

        harness.handleListChoice(player1, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(gravebane.getCard().getId());
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(splinters.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Bone Splinters");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
