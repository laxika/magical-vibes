package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FeastOfDreams;
import com.github.laxika.magicalvibes.cards.r.RottedHulk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SightlessBrawler.class, RottedHulk.class, FeastOfDreams.class})
class SightlessBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Sightless Brawler can be cast normally and can't attack alone")
    void castsNormallyAndCantAttackAlone() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SightlessBrawler()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent brawler = gd.playerBattlefields.get(player1.getId()).getFirst();
        brawler.setSummoningSick(false);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bestow gives the enchanted creature +3/+2 and prevents it from attacking alone")
    void bestowBoostsAndPreventsAloneAttack() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        harness.setHand(player1, List.of(new SightlessBrawler()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(7);

        host.setSummoningSick(false);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sightless Brawler's attack restriction does not prevent blocking alone")
    void canBlockAlone() {
        Permanent attacker = addCreatureReady(player1, new RottedHulk());
        attacker.setAttacking(true);

        Permanent brawler = addCreatureReady(player2, new SightlessBrawler());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(brawler.isBlocking()).isTrue();
    }

    @Test
    void canAttackWithAnotherCreature() {
        Permanent brawler = addCreatureReady(player1, new SightlessBrawler());
        Permanent companion = addCreatureReady(player1, new RottedHulk());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0, 1)));

        assertThat(brawler.isAttacking()).isTrue();
        assertThat(companion.isAttacking()).isTrue();
    }

    @Test
    void bestowedCreatureCanAttackWithAnotherCreature() {
        Permanent host = addCreatureReady(player1, new RottedHulk());
        Permanent companion = addCreatureReady(player1, new SightlessBrawler());
        harness.setHand(player1, List.of(new SightlessBrawler()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0, 1)));

        assertThat(host.isAttacking()).isTrue();
        assertThat(companion.isAttacking()).isTrue();
    }

    @Test
    void bestowedCreatureCanBlockAlone() {
        Permanent attacker = addCreatureReady(player1, new RottedHulk());
        Permanent host = addCreatureReady(player2, new RottedHulk());
        harness.setHand(player1, List.of(new SightlessBrawler()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(host.isBlocking()).isTrue();
    }

    @Test
    void bestowCanEnchantOpponentsCreatureAndRestrictsItsAttack() {
        Permanent host = addCreatureReady(player2, new RottedHulk());
        harness.setHand(player1, List.of(new SightlessBrawler()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(7);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void becomesCreatureWithAttackRestrictionWhenHostDies() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new RottedHulk());
        harness.setHand(player1, List.of(new SightlessBrawler(), new FeastOfDreams()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        Permanent brawler = findPermanent(player1, "Sightless Brawler");

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, host.getId());

        harness.assertInGraveyard(player1, "Rotted Hulk");
        harness.assertOnBattlefield(player1, "Sightless Brawler");
        assertThat(brawler.isAttached()).isFalse();
        brawler.setSummoningSick(false);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvesAsCreatureWhenBestowTargetDiesBeforeResolution() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new SightlessBrawler());
        harness.setHand(player1, List.of(new SightlessBrawler(), new FeastOfDreams()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, host.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent brawler = findPermanent(player1, "Sightless Brawler");
        assertThat(brawler.isAttached()).isFalse();
        brawler.setSummoningSick(false);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
