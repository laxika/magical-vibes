package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.s.ScytheTiger;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gomazoa.class, WelkinTern.class, ScytheTiger.class, IntoTheRoil.class, CosisTrickster.class})
class GomazoaTest extends BaseCardTest {

    @Test
    @DisplayName("Tucking Gomazoa also tucks each creature it is blocking")
    void tucksSourceAndBlockedCreature() {
        Permanent attacker = addCreatureReady(player1, new WelkinTern());
        Permanent gomazoa = addCreatureReady(player2, new Gomazoa());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(gomazoa);
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(card -> card.getName()))
                .contains("Welkin Tern");
        assertThat(gd.playerDecks.get(player2.getId()).stream().map(card -> card.getName()))
                .contains("Gomazoa");
    }

    @Test
    @DisplayName("Gomazoa can be activated when it is not blocking")
    void tucksOnlyItselfWhenNotBlocking() {
        Permanent gomazoa = addCreatureReady(player1, new Gomazoa());
        Permanent otherCreature = addCreatureReady(player1, new ScytheTiger());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(gomazoa);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherCreature);
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(card -> card.getName()))
                .contains("Gomazoa");
    }

    @Test
    void stillTucksBlockedCreatureWhenSourceIsReturnedToHand() {
        Permanent attacker = addCreatureReady(player1, new WelkinTern());
        Permanent gomazoa = addCreatureReady(player2, new Gomazoa());
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.activateAbility(player2, 0, null, null);
        harness.castInstant(player1, 0, gomazoa.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Gomazoa");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(card -> card.getName()))
                .contains("Welkin Tern");
        harness.assertInHand(player2, "Gomazoa");
        assertThat(gd.playerDecks.get(player2.getId()).stream().map(card -> card.getName()))
                .doesNotContain("Gomazoa");
    }

    @Test
    void doesNotTuckBlockedCreatureThatHasAlreadyLeftBattlefield() {
        Permanent attacker = addCreatureReady(player1, new WelkinTern());
        Permanent gomazoa = addCreatureReady(player2, new Gomazoa());
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.activateAbility(player2, 0, null, null);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Welkin Tern");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(gomazoa);
        assertThat(gd.playerDecks.get(player2.getId()).stream().map(card -> card.getName()))
                .contains("Gomazoa");
        harness.assertInHand(player1, "Welkin Tern");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(card -> card.getName()))
                .doesNotContain("Welkin Tern");
    }

    @Test
    void canTuckBlockedCreatureWithShroud() {
        Permanent attacker = addCreatureReady(player1, new ScytheTiger());
        Permanent gomazoa = addCreatureReady(player2, new Gomazoa());
        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(gomazoa);
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(card -> card.getName()))
                .contains("Scythe Tiger");
    }

    @Test
    void activationTapsSourceAndCannotBeRepeatedWhileTapped() {
        Permanent gomazoa = addCreatureReady(player1, new Gomazoa());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gomazoa.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Gomazoa");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent gomazoa = addCreatureReady(player1, new Gomazoa());
        gomazoa.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gomazoa.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationOutsideCombatShufflesOnlySourcesOwnersLibrary() {
        addCreatureReady(player1, new Gomazoa());
        Permanent ownTrickster = addCreatureReady(player1, new CosisTrickster());
        Permanent opposingTrickster = addCreatureReady(player2, new CosisTrickster());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(opposingTrickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownTrickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Gomazoa");
    }
}
