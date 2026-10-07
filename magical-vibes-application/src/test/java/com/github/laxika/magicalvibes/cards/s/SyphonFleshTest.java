package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornetQueen;
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

@CardUsed({SyphonFlesh.class, GrizzlyBears.class, HornetQueen.class})
@DisplayName("Syphon Flesh")
class SyphonFleshTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices a creature and the caster creates a Zombie for each one")
    void sacrificesEachOpponentCreatureAndCreatesTokens() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castSyphonFlesh();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownCreature.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
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
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        castSyphonFlesh();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(first.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(second.getId()));
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
