package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KellansLightblades.class, DarksteelRelic.class, GrizzlyBears.class, HillGiant.class})
class KellansLightbladesTest extends BaseCardTest {

    @Test
    void withoutBargainDealsThreeDamageToAnAttackingCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        target.setToughnessModifier(2);
        declareAttackers(List.of(0));

        castLightblades(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    void withoutBargainDealsThreeDamageToABlockingCreature() {
        addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setToughnessModifier(2);
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        castLightblades(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void bargainDestroysTheAttackingCreatureInsteadOfDealingDamage() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        target.setToughnessModifier(2);
        declareAttackers(List.of(1));
        harness.setHand(player1, List.of(new KellansLightblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetACreatureThatIsNotAttackingOrBlocking() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KellansLightblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castLightblades(UUID targetId) {
        harness.setHand(player1, List.of(new KellansLightblades()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
