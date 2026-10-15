package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QasaliPridemage.class, GrizzlyBears.class, LeoninScimitar.class, GloriousAnthem.class})
class QasaliPridemageTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new QasaliPridemage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1)); // Grizzly Bears attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new QasaliPridemage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("{1}, Sacrifice: destroys target artifact and sacrifices Qasali Pridemage")
    void destroysTargetArtifact() {
        addCreatureReady(player1, new QasaliPridemage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Sacrificed immediately as a cost
        harness.assertNotOnBattlefield(player1, "Qasali Pridemage");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("{1}, Sacrifice: destroys target enchantment")
    void destroysTargetEnchantment() {
        addCreatureReady(player1, new QasaliPridemage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new QasaliPridemage());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new QasaliPridemage());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsItselfWhenAttackingAlone() {
        Permanent pridemage = addCreatureReady(player1, new QasaliPridemage());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pridemage)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pridemage)).isEqualTo(3);
    }

    @Test
    void doesNotBoostAnOpponentsLoneAttacker() {
        addCreatureReady(player1, new QasaliPridemage());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateWhileTapped() {
        Permanent pridemage = addCreatureReady(player1, new QasaliPridemage());
        pridemage.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Qasali Pridemage");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void multipleExaltedAbilitiesBoostTheSameAttacker() {
        addCreatureReady(player1, new QasaliPridemage());
        addCreatureReady(player1, new QasaliPridemage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void canActivateWhileSummoningSickAndTargetOwnArtifact() {
        harness.addToBattlefield(player1, new QasaliPridemage());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Qasali Pridemage");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    void exaltedStillResolvesAfterItsSourceIsSacrificed() {
        addCreatureReady(player1, new QasaliPridemage());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(1));
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Qasali Pridemage");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }
}
