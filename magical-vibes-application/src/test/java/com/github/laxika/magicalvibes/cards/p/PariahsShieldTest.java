package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PariahsShield.class, GrizzlyBears.class})
class PariahsShieldTest extends BaseCardTest {

    @Test
    void equipAbilityAttachesShieldToTargetCreature() {
        Permanent shield = addShieldReady();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void damageToControllerIsRedirectedToEquippedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent shield = addShieldReady(player2);
        shield.setAttachedTo(creature.getId());
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    void unattachedShieldDoesNotRedirectDamage() {
        addShieldReady(player2);
        addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
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
