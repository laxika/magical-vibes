package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LionSash.class, DarksteelCitadel.class, Cancel.class, GrizzlyBears.class})
class LionSashTest extends BaseCardTest {

    @Test
    void exilingPermanentCardPutsCounterOnLionSash() {
        Permanent sash = addSash();
        Card citadel = new DarksteelCitadel();
        harness.setGraveyard(player2, new ArrayList<>(List.of(citadel)));
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateExileAbility(sash, citadel);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(citadel);
        assertThat(sash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void exilingNonpermanentCardDoesNotPutCounterOnLionSash() {
        Permanent sash = addSash();
        Card cancel = new Cancel();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cancel)));
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateExileAbility(sash, cancel);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cancel);
        assertThat(sash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void equippedCreatureGetsPlusOneForEachCounterOnLionSash() {
        Permanent sash = addSash();
        sash.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        sash.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void reconfigureAttachesAndUnattachesLionSash() {
        Permanent sash = addSash();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(sash.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.isCreature(gd, sash)).isFalse();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(sash.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, sash)).isTrue();
    }

    @Test
    void reconfigureCannotTargetOpponentsCreature() {
        Permanent sash = addSash();
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sash.getAttachedTo()).isNull();
    }

    private void activateExileAbility(Permanent sash, Card target) {
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(sash);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);
    }

    private Permanent addSash() {
        Permanent sash = new Permanent(new LionSash());
        sash.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sash);
        return sash;
    }
}
