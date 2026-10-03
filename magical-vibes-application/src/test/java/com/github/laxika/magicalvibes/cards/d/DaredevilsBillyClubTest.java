package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaredevilsBillyClub.class, GrizzlyBears.class})
class DaredevilsBillyClubTest extends BaseCardTest {

    @Test
    void enteringDealsDamageToTargetAndItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, java.util.List.of(new DaredevilsBillyClub()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void equipBoostsCreatureAndGrantsMenace() {
        Permanent club = harness.addToBattlefieldAndReturn(player1, new DaredevilsBillyClub());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(club.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    void enteringCanTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, java.util.List.of(new DaredevilsBillyClub()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    void illegalEnterTargetAlsoPreventsDamageToYou() {
        harness.setLife(player1, 20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new DaredevilsBillyClub()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Daredevil's Billy Club");
    }

    @Test
    void reequippingMovesBothBenefitsToNewCreature() {
        Permanent club = harness.addToBattlefieldAndReturn(player1, new DaredevilsBillyClub());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(club.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();
    }
}
