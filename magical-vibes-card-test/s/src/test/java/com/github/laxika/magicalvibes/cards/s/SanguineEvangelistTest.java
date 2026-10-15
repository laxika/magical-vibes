package com.github.laxika.magicalvibes.cards.s;

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
        assertThat(bat.getEffectivePower()).isEqualTo(1);
        assertThat(bat.getEffectiveToughness()).isEqualTo(1);
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

        assertThat(evangelist.getPowerModifier()).isZero();
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
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

        assertThat(evangelist.getPowerModifier()).isZero();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(nonattacker.getPowerModifier()).isZero();
        assertThat(opponent.getPowerModifier()).isZero();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(attacker.getPowerModifier()).isZero();
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
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isZero();
        Permanent bat = findPermanent(player1, "Bat");
        assertThat(bat.isAttacking()).isFalse();
        assertThat(bat.getEffectivePower()).isEqualTo(1);
        assertThat(bat.getEffectiveToughness()).isEqualTo(1);
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

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }
}
