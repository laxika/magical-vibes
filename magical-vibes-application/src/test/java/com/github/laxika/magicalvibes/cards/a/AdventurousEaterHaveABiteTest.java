package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdventurousEaterHaveABiteTest extends BaseCardTest {

    

    @Test
    @DisplayName("Entering the battlefield prepares Adventurous Eater and exiles a castable Have a Bite copy")
    void entersPrepared() {
        Permanent eater = castAdventurousEater();

        assertThat(eater.isPrepared()).isTrue();
        UUID copyId = eater.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Casting the prepared Have a Bite copy unprepares Adventurous Eater, adds a counter, and gains life")
    void castingPrepareCopyUnpreparesAndResolvesSpell() {
        Permanent eater = castAdventurousEater();
        Permanent target = addReady(player1, new GrizzlyBears());
        UUID copyId = eater.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(eater.isPrepared()).isFalse();
        assertThat(eater.getPreparedSpellCardId()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("When prepared Adventurous Eater leaves the battlefield, the exiled copy ceases to exist")
    void leavingBattlefieldRemovesExiledCopy() {
        Permanent eater = castAdventurousEater();
        UUID copyId = eater.getPreparedSpellCardId();
        assertThat(gd.findExiledCard(copyId)).isNotNull();

        eater.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(eater);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    private Permanent castAdventurousEater() {
        harness.setHand(player1, List.of(new AdventurousEaterHaveABite()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB BecomePrepared trigger

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Adventurous Eater"))
                .findFirst()
                .orElseThrow();
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player,
                               com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
