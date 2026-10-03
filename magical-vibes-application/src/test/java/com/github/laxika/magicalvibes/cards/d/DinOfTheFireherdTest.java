package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CemeteryPuca;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DinOfTheFireherd.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        ScatheZombies.class, Swamp.class, CemeteryPuca.class})
class DinOfTheFireherdTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 5/5 black and red Elemental token under the caster's control")
    void createsElementalToken() {
        castDin(player2.getId());

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
    }


    @Test
    @DisplayName("The token alone (black and red) forces one creature and one land sacrifice")
    void tokenAloneForcesOneCreatureAndOneLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());

        castDin(player2.getId());

        // Token counts as both a black and a red creature you control -> 1 creature + 1 land.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Swamp");
        harness.assertInGraveyard(player2, "Swamp");
    }

    @Test
    @DisplayName("Extra black and red creatures increase the number of sacrifices")
    void scalesWithBlackAndRedCreatures() {
        harness.addToBattlefield(player1, new ScatheZombies()); // black creature
        harness.addToBattlefield(player1, new HillGiant());     // red creature

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Forest());

        castDin(player2.getId());

        // Black creatures you control = ScatheZombies + token = 2 -> 2 creatures sacrificed.
        // Red creatures you control = HillGiant + token = 2 -> 2 lands sacrificed.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Swamp");
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Opponent chooses which creatures to sacrifice when they have more than required")
    void opponentChoosesWhenMoreCreaturesThanRequired() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());

        castDin(player2.getId());

        // Only the token is black -> sacrifice exactly 1 of the 2 creatures (opponent's choice).
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent chosen = findPermanent(player2, "Grizzly Bears");
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));

        // One creature remains; the land is auto-sacrificed by the red count of 1 (the token).
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
        harness.assertNotOnBattlefield(player2, "Swamp");
        harness.assertInGraveyard(player2, "Swamp");
    }

    @Test
    void automaticallySacrificedCreaturesDieSimultaneously() {
        harness.addToBattlefield(player1, new ScatheZombies());
        harness.addToBattlefield(player2, new CemeteryPuca());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDin(player2.getId());

        harness.assertInGraveyard(player2, "Cemetery Puca");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Puca must see the Bear die even though Puca dies in the same sacrifice event.
        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    void opponentChoosesLandAfterSacrificingCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Forest());

        castDin(player2.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Forest")));

        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player2, "Swamp");
    }

    @Test
    void sacrificesAvailablePermanentsWhenCountsExceedSupply() {
        harness.addToBattlefield(player1, new ScatheZombies());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        castDin(player2.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void ignoresGreenCreaturesAndOpponentsColoredCreaturesWhenCounting() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ScatheZombies());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Swamp());

        castDin(player2.getId());

        PendingInteraction.MultiPermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(creatureChoice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Scathe Zombies")));
        PendingInteraction.MultiPermanentChoice landChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(landChoice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Forest")));

        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Swamp");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target yourself — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new DinOfTheFireherd()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    private void castDin(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new DinOfTheFireherd()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
