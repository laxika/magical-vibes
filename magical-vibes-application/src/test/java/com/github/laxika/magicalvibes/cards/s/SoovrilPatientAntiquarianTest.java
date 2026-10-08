package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GraveyardShovel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoovrilPatientAntiquarian.class, GraveyardShovel.class, GrizzlyBears.class, Plains.class})
class SoovrilPatientAntiquarianTest extends BaseCardTest {

    @Test
    void putsTwoCountersOnOwnCardExiledFromGraveyardAndGivesItSuspend() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        addCreatureReady(player1, new SoovrilPatientAntiquarian());
        harness.activateAbility(player1, 1, null, target.getId(), Zone.EXILE);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 2);
        assertThat(gd.exiledCardsWithNonSuspendTimeCounters).doesNotContain(target.getId());
    }

    @Test
    void addsCountersToAnAlreadySuspendedCard() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        gd.exiledCardTimeCounters.put(target.getId(), 1);

        addCreatureReady(player1, new SoovrilPatientAntiquarian());
        harness.activateAbility(player1, 1, null, target.getId(), Zone.EXILE);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 3);
    }

    @Test
    void cannotTargetCardNotExiledFromOwnGraveyardThisTurn() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setExile(player1, List.of(target));
        addCreatureReady(player1, new SoovrilPatientAntiquarian());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetLandExiledFromOwnGraveyardThisTurn() {
        Plains target = new Plains();
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        addCreatureReady(player1, new SoovrilPatientAntiquarian());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, target.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
    }

    @Test
    void cannotTargetCardExiledFromOpponentsGraveyardThisTurn() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        addCreatureReady(player1, new SoovrilPatientAntiquarian());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, target.getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
    }

    @Test
    void grantedSuspendCountsDownOnlyOnOwnersUpkeepAndCastsWithoutMana() {
        GrizzlyBears target = new GrizzlyBears();
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        addCreatureReady(player1, new SoovrilPatientAntiquarian());
        harness.activateAbility(player1, 1, null, target.getId(), Zone.EXILE);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(target);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard()).isSameAs(target));
    }
}
