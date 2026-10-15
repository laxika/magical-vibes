package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GratuitousViolence.class, GoblinSharpshooter.class, GlorySeeker.class, Shock.class,
        Humility.class, Opalescence.class})
class GratuitousViolenceTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles noncombat damage dealt by a creature you control")
    void doublesNoncombatDamageDealtByControlledCreature() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        addCreatureReady(player1, new GoblinSharpshooter());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles noncombat damage dealt by a creature you control to a permanent")
    void doublesNoncombatDamageDealtByControlledCreatureToPermanent() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        addCreatureReady(player1, new GoblinSharpshooter());
        Permanent target = addCreatureReady(player2, new GlorySeeker());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Doubles combat damage dealt by a creature you control")
    void doublesCombatDamageDealtByControlledCreature() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GratuitousViolence());
        addCreatureReady(player1, new GlorySeeker());

        declareAttackers(List.of(1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not double damage from a noncreature source")
    void doesNotDoubleDamageFromNoncreatureSource() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double damage dealt by an opponent's creature")
    void doesNotDoubleDamageDealtByOpponentsCreature() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        Permanent attacker = addCreatureReady(player2, new GoblinSharpshooter());
        attacker.setAttacking(true);
        harness.setLife(player1, 20);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Two copies multiply controlled creature damage by four")
    void multipleCopiesMultiplyDamage() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        harness.addToBattlefield(player1, new GratuitousViolence());
        addCreatureReady(player1, new GoblinSharpshooter());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Doubles damage to a creature controlled by the same player")
    void doublesDamageToOwnCreature() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        addCreatureReady(player1, new GoblinSharpshooter());
        Permanent target = addCreatureReady(player1, new GlorySeeker());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Doubles damage to the source creature's controller")
    void doublesDamageToOwnPlayer() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        addCreatureReady(player1, new GoblinSharpshooter());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Uses last known information when the creature dies before its ability resolves")
    void doublesDamageFromCreatureThatLeftBattlefield() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        Permanent source = addCreatureReady(player1, new GoblinSharpshooter());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.castInstant(player2, 0, source.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Goblin Sharpshooter");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not double damage after animated Gratuitous Violence loses its abilities")
    void abilityRemovalDisablesDamageDoubling() {
        harness.addToBattlefield(player1, new GratuitousViolence());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new Humility());
        addCreatureReady(player1, new GlorySeeker());
        harness.setLife(player2, 20);

        declareAttackers(List.of(3));
        resolveCombat();

        harness.assertLife(player2, 19);
    }
}
