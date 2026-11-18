package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguineEvangelist.class, GrizzlyBears.class, Shock.class})
class SanguineEvangelistTest extends BaseCardTest {

    @Test
    @DisplayName("When Sanguine Evangelist enters, it creates a 1/1 black Bat token with flying")
    void entersCreatesBatToken() {
        castEvangelist();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent bat = findPermanent(player1, "Bat");
        assertThat(bat.getCard().isToken()).isTrue();
        assertThat(bat.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(bat.getCard().getSubtypes()).contains(CardSubtype.BAT);
        assertThat(bat.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.getEffectivePower(gd, bat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bat)).isEqualTo(1);
    }

    @Test
    @DisplayName("When Sanguine Evangelist dies, it creates a Bat token")
    void diesCreatesBatToken() {
        Permanent evangelist = addCreatureReady(player1, new SanguineEvangelist());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, evangelist.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bat")).hasSize(1);
    }

    @Test
    @DisplayName("Battle Cry gives +1/+0 to other attacking creatures")
    void battleCryBoostsOtherAttackers() {
        Permanent evangelist = addCreatureReady(player1, new SanguineEvangelist());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, evangelist)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    private void castEvangelist() {
        harness.castFromHand(player1, new SanguineEvangelist(), "{2}{W}");
        harness.passBothPriorities();
    }

    @Test
    void enterAndDeathTriggersBothResolveAfterEvangelistDies() {
        castEvangelist();
        Permanent evangelist = findPermanent(player1, "Sanguine Evangelist");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, evangelist.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Sanguine Evangelist");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bat")).hasSize(2);
        assertThat(findPermanents(player2, "Bat")).isEmpty();
    }

    @Test
    void battleCryExcludesNonattackersAndExpiresAtCleanup() {
        Permanent evangelist = addCreatureReady(player1, new SanguineEvangelist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(PermanentGrants.powerBoostOf(gd, evangelist)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, nonattacker)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opponent)).isZero();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passUntil(TurnStep.END_STEP);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isZero();
    }

    @Test
    void battleCryStillResolvesAfterEvangelistDiesWithoutBoostingNewBat() {
        Permanent evangelist = addCreatureReady(player1, new SanguineEvangelist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.passPriority(player1);
            harness.castInstant(player2, 0, evangelist.getId());
            harness.passBothPriorities();
            resolveAllTriggers();
        });

        harness.assertNotOnBattlefield(player1, "Sanguine Evangelist");
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
        Permanent bat = findPermanent(player1, "Bat");
        assertThat(bat.isAttacking()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bat)).isEqualTo(1);
    }

    @Test
    void twoEvangelistsBoostEachOtherAndStackOnAnotherAttacker() {
        Permanent first = addCreatureReady(player1, new SanguineEvangelist());
        Permanent second = addCreatureReady(player1, new SanguineEvangelist());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1, 2));
            resolveAllTriggers();
        });

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, attacker)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, attacker)).isZero();
    }
}
