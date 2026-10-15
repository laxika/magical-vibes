package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BoneMask;
import com.github.laxika.magicalvibes.cards.d.DwarvenMiner;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FieryEmancipation;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.p.PhyrexianDreadnought;
import com.github.laxika.magicalvibes.cards.l.LionsEyeDiamond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTinkerer.class, BoneMask.class, LionsEyeDiamond.class, DwarvenMiner.class,
        Disenchant.class, Incinerate.class, PhyrexianDreadnought.class, FieryEmancipation.class})
class GoblinTinkererTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and takes damage equal to its mana value")
    void destroysArtifactAndTakesManaValueDamage() {
        Permanent tinkerer = addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player2, new BoneMask()); // {4}, mana value 4
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Bone Mask");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bone Mask");
        // 4 damage to a 1/2 is lethal
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tinkerer);
        harness.assertInGraveyard(player1, "Goblin Tinkerer");
    }

    @Test
    @DisplayName("Survives when the destroyed artifact's mana value is 0")
    void survivesZeroManaValueArtifact() {
        Permanent tinkerer = addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player2, new LionsEyeDiamond()); // {0}, mana value 0
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Lion's Eye Diamond");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lion's Eye Diamond");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tinkerer);
        assertThat(tinkerer.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-artifact permanent")
    void cannotTargetNonArtifact() {
        addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player2, new DwarvenMiner());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Dwarven Miner");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDestroyOwnArtifactAndSurviveOneDamage() {
        Permanent tinkerer = addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player1, new PhyrexianDreadnought());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Phyrexian Dreadnought"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Phyrexian Dreadnought");
        harness.assertOnBattlefield(player1, "Goblin Tinkerer");
        assertThat(tinkerer.getMarkedDamage()).isEqualTo(1);
        assertThat(tinkerer.isTapped()).isTrue();
    }

    @Test
    void artifactStillDealsDamageWhenItRegenerates() {
        addCreatureReady(player1, new GoblinTinkerer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BoneMask());
        artifact.setRegenerationShield(1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Bone Mask");
        harness.assertNotInGraveyard(player2, "Bone Mask");
        assertThat(artifact.isTapped()).isTrue();
        assertThat(artifact.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Goblin Tinkerer");
    }

    @Test
    void noDamageWhenArtifactIsDestroyedInResponse() {
        Permanent tinkerer = addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player2, new BoneMask());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        UUID artifactId = harness.getPermanentId(player2, "Bone Mask");
        harness.activateAbility(player1, 0, null, artifactId);
        harness.castAndResolveInstant(player2, 0, artifactId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Bone Mask");
        harness.assertOnBattlefield(player1, "Goblin Tinkerer");
        assertThat(tinkerer.getMarkedDamage()).isZero();
    }

    @Test
    void stillDestroysArtifactWhenTinkererDiesInResponse() {
        Permanent tinkerer = addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player2, new BoneMask());
        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Bone Mask"));
        harness.castAndResolveInstant(player2, 0, tinkerer.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Goblin Tinkerer");
        harness.assertInGraveyard(player2, "Bone Mask");
    }

    @Test
    void tinkererControllersEmancipationDoesNotTripleOpponentsArtifactDamage() {
        Permanent tinkerer = addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player1, new FieryEmancipation());
        harness.addToBattlefield(player2, new PhyrexianDreadnought());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Phyrexian Dreadnought"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Phyrexian Dreadnought");
        harness.assertOnBattlefield(player1, "Goblin Tinkerer");
        assertThat(tinkerer.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void artifactControllersEmancipationTriplesDamageAfterArtifactIsDestroyed() {
        addCreatureReady(player1, new GoblinTinkerer());
        harness.addToBattlefield(player2, new FieryEmancipation());
        harness.addToBattlefield(player2, new PhyrexianDreadnought());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Phyrexian Dreadnought"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Phyrexian Dreadnought");
        harness.assertInGraveyard(player1, "Goblin Tinkerer");
    }
}
