package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DwarvenHammer;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarchanterSkald.class, Rancor.class, DwarvenHammer.class})
class WarchanterSkaldTest extends BaseCardTest {

    @Test
    void createsDwarfBerserkerWhenEnchantedCreatureBecomesTapped() {
        Permanent skald = addCreatureReady(player1, new WarchanterSkald());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Rancor());
        aura.setAttachedTo(skald.getId());

        tapAndResolve(skald);

        assertThat(findPermanents(player1, "Dwarf Berserker")).hasSize(1);
    }

    @Test
    void createsDwarfBerserkerWhenEquippedCreatureBecomesTapped() {
        Permanent skald = addCreatureReady(player1, new WarchanterSkald());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new DwarvenHammer());
        equipment.setAttachedTo(skald.getId());

        tapAndResolve(skald);

        assertThat(findPermanents(player1, "Dwarf Berserker")).hasSize(1);
    }

    @Test
    void doesNotCreateDwarfBerserkerWhenUnattachedCreatureBecomesTapped() {
        Permanent skald = addCreatureReady(player1, new WarchanterSkald());

        tapAndResolve(skald);

        assertThat(findPermanents(player1, "Dwarf Berserker")).isEmpty();
    }

    @Test
    void doesNotResolveTriggerAfterAttachmentIsRemoved() {
        Permanent skald = addCreatureReady(player1, new WarchanterSkald());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Rancor());
        aura.setAttachedTo(skald.getId());

        harness.tapPermanent(player1, battlefieldIndex(skald));
        aura.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dwarf Berserker")).isEmpty();
    }

    private void tapAndResolve(Permanent permanent) {
        harness.tapPermanent(player1, battlefieldIndex(permanent));
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
