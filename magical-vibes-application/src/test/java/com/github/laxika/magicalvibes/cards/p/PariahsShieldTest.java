package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RainOfEmbers;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PariahsShield.class, RainOfEmbers.class, Watchwolf.class})
class PariahsShieldTest extends BaseCardTest {

    @Test
    void equipAbilityAttachesShieldToTargetCreature() {
        Permanent shield = addShieldReady();
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void equipAbilityCannotTargetAnOpponentCreature() {
        Permanent shield = addShieldReady();
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(shield.getAttachedTo()).isNull();
    }

    @Test
    void damageToControllerIsRedirectedToEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        Permanent shield = addShieldReady(player2);
        shield.setAttachedTo(creature.getId());
        addCreatureReady(player1, new Watchwolf());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    void unattachedShieldDoesNotRedirectDamage() {
        addShieldReady(player2);
        addCreatureReady(player1, new Watchwolf());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void noncombatDamageToControllerIsRedirectedToEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        Permanent shield = addShieldReady(player2);
        shield.setAttachedTo(creature.getId());

        harness.castFromHand(player1, new RainOfEmbers(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addShieldReady() {
        return addShieldReady(player1);
    }

    private Permanent addShieldReady(Player player) {
        Permanent shield = new Permanent(new PariahsShield());
        gd.playerBattlefields.get(player.getId()).add(shield);
        return shield;
    }
}
