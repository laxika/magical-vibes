package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HornetQueen;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyphonFlesh.class, HornetQueen.class, TrollAscetic.class})
@DisplayName("Syphon Flesh")
class SyphonFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices a creature and the caster creates a Zombie for each one")
    void sacrificesEachOpponentCreatureAndCreatesTokens() {
        Permanent ownCreature = addCreatureReady(player1, new HornetQueen());
        addCreatureReady(player2, new HornetQueen());

        castSyphonFlesh();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownCreature.getId()));
        harness.assertInGraveyard(player2, "Hornet Queen");
        assertThat(zombieCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent without a creature does not contribute a Zombie")
    void opponentWithoutCreatureDoesNotCreateToken() {
        castSyphonFlesh();

        assertThat(zombieCount(player1)).isZero();
    }

    @Test
    @DisplayName("The opponent chooses which creature to sacrifice")
    void opponentChoosesCreature() {
        Permanent first = addCreatureReady(player2, new HornetQueen());
        Permanent second = addCreatureReady(player2, new HornetQueen());

        castSyphonFlesh();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Hornet Queen"))
                .contains(first).doesNotContain(second);
        assertThat(zombieCount(player1)).isEqualTo(1);
    }

    @Test
    @CardUsed({SyphonFlesh.class, HornetQueen.class})
    @DisplayName("Sacrificing a creature token also creates a 2/2 black Zombie")
    void sacrificingTokenCreatesZombie() {
        Permanent queen = harness.enterBattlefieldAndReturn(player2, new HornetQueen());
        resolveAllTriggers();
        Permanent insect = findPermanents(player2, "Insect").getFirst();
        assertThat(countPermanents(player2, "Insect")).isEqualTo(4);

        castSyphonFlesh();

        assertThat(zombieCount(player1)).isZero();
        harness.handleMultiplePermanentsChosen(player2, List.of(insect.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(insect.getId()))
                .anyMatch(permanent -> permanent.getId().equals(queen.getId()));
        assertThat(countPermanents(player2, "Insect")).isEqualTo(3);
        harness.assertNotInGraveyard(player2, "Insect");
        assertThat(zombieCount(player1)).isEqualTo(1);
        assertThat(zombieCount(player2)).isZero();
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.isTapped()).isFalse();
        assertThat(zombie.isSummoningSick()).isTrue();
    }

    @Test
    @CardUsed({SyphonFlesh.class, HornetQueen.class})
    @DisplayName("The caster's creatures are not sacrificed when the opponent has none")
    void onlyCasterHasCreatures() {
        addCreatureReady(player1, new HornetQueen());

        castSyphonFlesh();

        harness.assertOnBattlefield(player1, "Hornet Queen");
        harness.assertNotInGraveyard(player1, "Hornet Queen");
        assertThat(zombieCount(player1)).isZero();
        harness.assertInGraveyard(player1, "Syphon Flesh");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SyphonFlesh.class, TrollAscetic.class})
    @DisplayName("Hexproof does not prevent the non-targeted sacrifice")
    void sacrificesHexproofCreature() {
        addCreatureReady(player2, new TrollAscetic());

        castSyphonFlesh();

        harness.assertNotOnBattlefield(player2, "Troll Ascetic");
        harness.assertInGraveyard(player2, "Troll Ascetic");
        assertThat(zombieCount(player1)).isEqualTo(1);
        assertThat(zombieCount(player2)).isZero();
        harness.assertInGraveyard(player1, "Syphon Flesh");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castSyphonFlesh() {
        harness.castFromHand(player1, new SyphonFlesh(), "{4}{B}");
        harness.passBothPriorities();
    }

    private long zombieCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Zombie"))
                .count();
    }
}
