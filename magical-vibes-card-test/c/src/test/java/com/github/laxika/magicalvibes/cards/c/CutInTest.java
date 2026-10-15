package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CutIn.class, GrizzlyBears.class, SerraAngel.class, GiantGrowth.class, Unsummon.class})
class CutInTest extends BaseCardTest {

    @Test
    void dealsDamageAndAttachesYoungHeroRoleToTheSameCreature() {
        SerraAngel targetCard = new SerraAngel();
        targetCard.setToughness(5);
        Permanent target = addCreatureReady(player1, targetCard);
        harness.setHand(player1, List.of(new CutIn()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId(), target.getId()));

        Permanent role = findPermanent(player1, "Young Hero");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(role.getCard().getSubtypes()).contains(CardSubtype.ROLE);
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    void roleTargetCanBeOmitted() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CutIn()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Young Hero")).isEmpty();
    }

    @Test
    void youngHeroPutsCounterOnSmallCreatureWhenItAttacks() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent damageTarget = addCreatureReady(player2, new SerraAngel());
        harness.setHand(player1, List.of(new CutIn()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(damageTarget.getId(), target.getId()));

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void youngHeroPutsCounterOnCreatureWithExactlyThreeToughness() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent damageTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CutIn()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(damageTarget.getId(), target.getId()));
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void youngHeroDoesNotTriggerForCreatureWithFourToughness() {
        Permanent target = addCreatureReady(player1, new SerraAngel());
        Permanent damageTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CutIn()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(damageTarget.getId(), target.getId()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target))));

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void youngHeroRechecksToughnessWhenItsTriggerResolves() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent damageTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CutIn(), new GiantGrowth()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(damageTarget.getId(), target.getId()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target))));
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void newestYoungHeroRoleReplacesOlderRoleFromSameController() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CutIn(), new CutIn()));
        Permanent firstDamageTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondDamageTarget = addCreatureReady(player2, new GrizzlyBears());
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(firstDamageTarget.getId(), target.getId()));
        Permanent firstRole = findPermanent(player1, "Young Hero");

        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(secondDamageTarget.getId(), target.getId()));

        assertThat(findPermanents(player1, "Young Hero")).hasSize(1);
        Permanent newRole = findPermanent(player1, "Young Hero");
        assertThat(newRole.getId()).isNotEqualTo(firstRole.getId());
        assertThat(newRole.getAttachedTo()).isEqualTo(target.getId());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveAllTriggers();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void createsRoleWhenOnlyDamageTargetBecomesIllegal() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent damageTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CutIn(), new Unsummon()));
        addMana();
        harness.castSorcery(player1, 0, List.of(damageTarget.getId(), target.getId()));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, damageTarget.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Young Hero").getAttachedTo()).isEqualTo(target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void dealsDamageWhenOnlyRoleTargetBecomesIllegal() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent damageTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CutIn(), new Unsummon()));
        addMana();
        harness.castSorcery(player1, 0, List.of(damageTarget.getId(), target.getId()));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Young Hero")).isEmpty();
    }

    @Test
    void createsNoRoleWhenSharedTargetBecomesIllegal() {
        Permanent target = addCreatureReady(player1, new SerraAngel());
        harness.setHand(player1, List.of(new CutIn(), new Unsummon()));
        addMana();
        harness.castSorcery(player1, 0, List.of(target.getId(), target.getId()));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Serra Angel");
        harness.assertInGraveyard(player1, "Cut In");
        assertThat(findPermanents(player1, "Young Hero")).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
