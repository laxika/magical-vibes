package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaffOfTitania.class, Forest.class, GrizzlyBears.class})
class StaffOfTitaniaTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +X/+X for each Forest you control")
    void equippedCreatureScalesWithForests() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTitania());
        staff.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking with the equipped creature creates a Forest Dryad land creature")
    void attackTriggerCreatesForestDryad() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfTitania());
        staff.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent dryad = findPermanent(player1, "Forest Dryad");
        assertThat(gqs.isCreature(gd, dryad)).isTrue();
        assertThat(gqs.isLand(gd, dryad)).isTrue();
        assertThat(dryad.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.FOREST, CardSubtype.DRYAD);
        assertThat(dryad.getEffectivePower()).isEqualTo(1);
        assertThat(dryad.getEffectiveToughness()).isEqualTo(1);
        assertThat(dryad.isSummoningSick()).isTrue();
        assertThat(dryad.isTapped()).isFalse();
        assertThat(dryad.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("An unattached Staff of Titania does not trigger on attack")
    void unattachedStaffDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new StaffOfTitania());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Forest Dryad"));
    }
}
