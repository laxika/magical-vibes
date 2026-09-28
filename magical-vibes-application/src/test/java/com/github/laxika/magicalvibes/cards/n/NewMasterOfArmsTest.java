package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NewMasterOfArms.class, GiantSpider.class})
class NewMasterOfArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped blockers deal no combat damage")
    void tappedBlockerDealsNoCombatDamage() {
        Permanent master = addCreatureReady(player1, new NewMasterOfArms());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        declareBlocker(blocker);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        resolveCombat();

        assertThat(master.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Untapped blockers still deal combat damage")
    void untappedBlockerDealsCombatDamage() {
        Permanent master = addCreatureReady(player1, new NewMasterOfArms());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        declareBlocker(blocker);

        resolveCombat();

        assertThat(master.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking it")
    void nonBlockingCreatureCannotBeTargeted() {
        addCreatureReady(player1, new NewMasterOfArms());
        Permanent bystander = addCreatureReady(player2, new GiantSpider());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareBlocker(Permanent blocker) {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
    }
}
