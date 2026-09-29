package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantomSteed.class, GrizzlyBears.class})
class PhantomSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and exiles another creature you control until it leaves")
    void entersAndExilesAnotherCreatureUntilItLeaves() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent steed = castSteed(bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.getCardsExiledByPermanent(steed.getId())).containsExactly(bear.getCard());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, steed));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == bear.getCard());
    }

    @Test
    @DisplayName("Attacking creates a tapped and attacking Illusion copy that is sacrificed at end of combat")
    void attackingCreatesTemporaryIllusionCopy() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent steed = castSteed(bear.getId());
        steed.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(steed)));
            resolveAllTriggers();

            Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                    .filter(permanent -> permanent.getCard().isToken())
                    .findFirst()
                    .orElseThrow();
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ILLUSION);
        });

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PhantomSteed()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    private Permanent castSteed(UUID targetId) {
        harness.setHand(player1, List.of(new PhantomSteed()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Phantom Steed");
    }
}
