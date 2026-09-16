package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.p.PsionicBlast;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VodalianMystic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphereOfReason.class, ProdigalSorcerer.class, Shock.class, AirElemental.class, PsionicBlast.class, VodalianMystic.class})
class SphereOfReasonTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 2 damage from a blue noncombat source")
    void preventsBlueNoncombatDamage() {
        harness.addToBattlefield(player1, new SphereOfReason());
        addCreatureReady(player2, new ProdigalSorcerer());
        harness.setLife(player1, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage from a nonblue source")
    void doesNotPreventNonblueDamage() {
        harness.addToBattlefield(player1, new SphereOfReason());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Uses a spell's current color when it changes before dealing damage")
    void preventsDamageFromSpellThatBecomesBlue() {
        harness.addToBattlefield(player1, new SphereOfReason());
        addCreatureReady(player2, new VodalianMystic());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        Card targetSpell = gd.stack.getFirst().getCard();
        harness.activateAbility(player2, 0, 0, null, targetSpell.getId(), Zone.STACK);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BLUE");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents only 2 damage from each blue damage event")
    void preventsOnlyTwoDamageFromBlueSource() {
        harness.addToBattlefield(player1, new SphereOfReason());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new PsionicBlast()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Protects only its controller from blue damage")
    void protectsOnlyItsController() {
        harness.addToBattlefield(player1, new SphereOfReason());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PsionicBlast()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Prevents 2 damage from a single blue combat source")
    void preventsBlueCombatDamagePerSource() {
        harness.addToBattlefield(player1, new SphereOfReason());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new AirElemental());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents 2 damage from each blue combat source")
    void preventsTwoDamageFromEachBlueCombatSource() {
        harness.addToBattlefield(player1, new SphereOfReason());
        harness.setLife(player1, 20);

        addCreatureReady(player2, new AirElemental());
        addCreatureReady(player2, new AirElemental());
        declareAttackers(player2, List.of(0, 1));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }
}
