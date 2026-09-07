package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.r.RattleclawMystic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QarsiDeceiver.class, RattleclawMystic.class})
class QarsiDeceiverTest extends BaseCardTest {

    @Test
    void restrictedManaCannotCastNormalSpell() {
        addCreatureReady(player1, new QarsiDeceiver());
        harness.setHand(player1, List.of(new RattleclawMystic()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaCastsFaceDownSpellAndTurnsItFaceUp() {
        addCreatureReady(player1, new QarsiDeceiver());
        addCreatureReady(player1, new QarsiDeceiver());
        harness.setHand(player1, List.of(new RattleclawMystic()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mystic = findPermanent(player1, "Rattleclaw Mystic");
        assertThat(mystic.isFaceDown()).isTrue();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mystic));

        assertThat(mystic.isFaceDown()).isFalse();
    }
}
