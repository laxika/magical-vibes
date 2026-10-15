package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FinalShowdown;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZephidsEmbrace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErietteTheBeguiler.class, ZephidsEmbrace.class, GrizzlyBears.class, AirElemental.class,
        FinalShowdown.class})
class ErietteTheBeguilerTest extends BaseCardTest {

    @Test
    void eligibleAuraStealsOpponentPermanentUntilAuraDetaches() {
        addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castZephidsEmbrace(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        Permanent aura = findPermanent(player1, "Zephid's Embrace");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void auraDoesNotTriggerForHigherManaValueOpponentPermanent() {
        addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player2, new AirElemental());

        castZephidsEmbrace(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void auraStealsPermanentWithEqualManaValue() {
        Permanent eriette = addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player2, new ErietteTheBeguiler());

        castZephidsEmbrace(target);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, eriette));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void auraLeavingBeforeTriggerResolvesPreventsControlGain() {
        addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castZephidsEmbrace(target);
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Zephid's Embrace");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void triggerStillResolvesAfterErietteLeaves() {
        Permanent eriette = addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castZephidsEmbrace(target);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, eriette));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void controlPersistsAfterErietteLeaves() {
        Permanent eriette = addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castZephidsEmbrace(target);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, eriette));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void auraAttachedToOwnPermanentDoesNotTrigger() {
        addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castZephidsEmbrace(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void erietteDoesNotTriggerWhileItHasLostAllAbilities() {
        addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FinalShowdown()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0}, List.of());
        harness.passBothPriorities();

        castZephidsEmbrace(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void opponentsAuraDoesNotTriggerEriette() {
        addCreatureReady(player1, new ErietteTheBeguiler());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new ZephidsEmbrace()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void combatDamageGainsLife() {
        addCreatureReady(player1, new ErietteTheBeguiler());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    private void castZephidsEmbrace(Permanent target) {
        harness.setHand(player1, List.of(new ZephidsEmbrace()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
    }
}
