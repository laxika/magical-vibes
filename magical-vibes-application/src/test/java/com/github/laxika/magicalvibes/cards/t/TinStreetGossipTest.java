package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RattleclawMystic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TinStreetGossip.class, RattleclawMystic.class})
class TinStreetGossipTest extends BaseCardTest {

    @Test
    void restrictedManaCannotCastNormalSpell() {
        addCreatureReady(player1, new TinStreetGossip());
        harness.setHand(player1, List.of(new RattleclawMystic()));

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaCastsFaceDownSpellAndTurnsItFaceUp() {
        addCreatureReady(player1, new TinStreetGossip());
        addCreatureReady(player1, new TinStreetGossip());
        harness.setHand(player1, List.of(new RattleclawMystic()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mystic = findPermanent(player1, "Rattleclaw Mystic");
        assertThat(mystic.isFaceDown()).isTrue();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mystic));

        assertThat(mystic.isFaceDown()).isFalse();
    }
}
