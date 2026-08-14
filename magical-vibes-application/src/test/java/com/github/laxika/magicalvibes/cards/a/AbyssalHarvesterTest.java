package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbyssalHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature put into a graveyard this turn and creates a Nightmare copy")
    void createsNightmareCopyAndExilesOtherNightmares() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AbyssalHarvester());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card oldNightmare = token("Old Nightmare", CardSubtype.NIGHTMARE);
        Card zombieToken = token("Zombie", CardSubtype.ZOMBIE);
        harness.addToBattlefield(player1, oldNightmare);
        harness.addToBattlefield(player1, zombieToken);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dyingCreature));

        int harvesterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(harvester);
        harness.activateAbilityWithGraveyardTargets(player1, harvesterIndex, 0, List.of(dyingCreature.getCard().getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(dyingCreature.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(oldNightmare.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.NIGHTMARE)
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BEAR));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(zombieToken.getId()));
    }

    @Test
    @DisplayName("Rejects a creature card that was not put into a graveyard this turn")
    void rejectsCardNotPutIntoGraveyardThisTurn() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AbyssalHarvester());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        int harvesterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(harvester);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, harvesterIndex, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("put into a graveyard this turn");
    }

    @Test
    @DisplayName("Rejects a noncreature card even when it was put into a graveyard this turn")
    void rejectsNoncreatureCard() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AbyssalHarvester());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));

        int harvesterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(harvester);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, harvesterIndex, 0, List.of(land.getCard().getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    private Card token(String name, CardSubtype subtype) {
        Card token = new Card();
        token.setName(name);
        token.setToken(true);
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.BLACK);
        token.setColors(List.of(CardColor.BLACK));
        token.setSubtypes(List.of(subtype));
        token.setPower(1);
        token.setToughness(1);
        return token;
    }
}
