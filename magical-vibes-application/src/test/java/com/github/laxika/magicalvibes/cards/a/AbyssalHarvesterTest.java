package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.d.DazzlingAngel;
import com.github.laxika.magicalvibes.cards.e.EmpyreanEagle;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbyssalHarvester.class, BearCub.class, Plains.class, DazzlingAngel.class, EmpyreanEagle.class})
class AbyssalHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature put into a graveyard this turn and creates a Nightmare copy")
    void createsNightmareCopyAndExilesOtherNightmares() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AbyssalHarvester());
        harvester.setSummoningSick(false);
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player2, new BearCub());
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
        harvester.setSummoningSick(false);
        BearCub bears = new BearCub();
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
        harvester.setSummoningSick(false);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));

        int harvesterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(harvester);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, harvesterIndex, 0, List.of(land.getCard().getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    @Test
    void unavailableTargetDoesNotExileExistingNightmares() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AbyssalHarvester());
        harvester.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BearCub());
        Card nightmare = token("Old Nightmare", CardSubtype.NIGHTMARE);
        harness.addToBattlefield(player1, nightmare);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getCard().getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardByIdForExile(gd, creature.getCard().getId()));
        harness.setExile(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(nightmare.getId());
    }

    @Test
    void resolvesAfterHarvesterLeavesAndPreservesOpponentsNightmares() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AbyssalHarvester());
        harvester.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BearCub());
        Card opposingNightmare = token("Opposing Nightmare", CardSubtype.NIGHTMARE);
        harness.addToBattlefield(player2, opposingNightmare);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getCard().getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, harvester));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.NIGHTMARE, CardSubtype.BEAR);
                });
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(opposingNightmare.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
    }

    @Test
    void copiedCreatureRetainsItsTriggeredAbility() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AbyssalHarvester());
        harvester.setSummoningSick(false);
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new DazzlingAngel());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, angel));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(angel.getCard().getId()));
        harness.passBothPriorities();

        harness.enterBattlefieldAndReturn(player1, new BearCub());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void copiedMulticoloredCreatureRetainsAllItsColors() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AbyssalHarvester());
        harvester.setSummoningSick(false);
        Permanent eagle = harness.addToBattlefieldAndReturn(player2, new EmpyreanEagle());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, eagle));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(eagle.getCard().getId()));
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(harness.getGameQueryService().getEffectiveColors(gd, copy))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
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
