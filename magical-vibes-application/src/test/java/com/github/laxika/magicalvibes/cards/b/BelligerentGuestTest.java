package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BelligerentGuest.class, SerraAngel.class})
class BelligerentGuestTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Blood token when it deals combat damage to a player")
    void createsBloodTokenOnCombatDamageToPlayer() {
        Permanent guest = addReadyCreature(player1, new BelligerentGuest());
        guest.setAttacking(true);

        resolveCombat();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Blood token when blocked without dealing player damage")
    void doesNotCreateBloodTokenWhenBlocked() {
        Permanent guest = addReadyCreature(player1, new BelligerentGuest());
        guest.setAttacking(true);

        Permanent blocker = addReadyCreature(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
