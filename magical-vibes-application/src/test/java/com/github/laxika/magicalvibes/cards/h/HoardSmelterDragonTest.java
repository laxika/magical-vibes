package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HoardSmelterDragonTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has activated ability with destroy-artifact-and-boost effect and artifact target filter")
    void hasCorrectAbility() {
        HoardSmelterDragon card = new HoardSmelterDragon();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{3}{R}");
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).singleElement()
                .isInstanceOf(DestroyTargetPermanentAndBoostSelfByManaValueEffect.class);
        assertThat(ability.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        PermanentPredicateTargetFilter filter = (PermanentPredicateTargetFilter) ability.getTargetFilter();
        assertThat(filter.predicate()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    // ===== Destroy artifact and boost =====

    @Test
    @DisplayName("Destroys target artifact and gets +X/+0 where X is that artifact's mana value")
    void destroysArtifactAndBoostsSelf() {
        Permanent dragon = addDragon(player1);
        harness.addToBattlefield(player2, new RodOfRuin()); // MV = 4
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Rod of Ruin is destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Rod of Ruin"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Rod of Ruin"));

        // Dragon gets +4/+0 (Rod of Ruin MV = 4), so effective power = 5 + 4 = 9
        assertThat(dragon.getEffectivePower()).isEqualTo(9);
        assertThat(dragon.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Destroying a 0-MV artifact does not boost power")
    void zeroManaValueDoesNotBoost() {
        Permanent dragon = addDragon(player1);
        harness.addToBattlefield(player2, new FountainOfYouth()); // MV = 0
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Fountain of Youth is destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fountain of Youth"));

        // Dragon power unchanged (MV = 0)
        assertThat(dragon.getEffectivePower()).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost stacks when ability is activated multiple times")
    void boostStacks() {
        Permanent dragon = addDragon(player1);
        harness.addToBattlefield(player2, new LeoninScimitar()); // MV = 1
        harness.addToBattlefield(player2, new RodOfRuin());      // MV = 4
        harness.addMana(player1, ManaColor.RED, 8);

        // Destroy Leonin Scimitar first
        UUID scimitarId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.activateAbility(player1, 0, null, scimitarId);
        harness.passBothPriorities();

        assertThat(dragon.getEffectivePower()).isEqualTo(6); // 5 + 1

        // Destroy Rod of Ruin second
        UUID rodId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.activateAbility(player1, 0, null, rodId);
        harness.passBothPriorities();

        assertThat(dragon.getEffectivePower()).isEqualTo(10); // 5 + 1 + 4
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target a creature with the ability")
    void cannotTargetCreature() {
        addDragon(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 4);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target artifact is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent dragon = addDragon(player1);
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.activateAbility(player1, 0, null, targetId);

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Dragon gets no boost
        assertThat(dragon.getEffectivePower()).isEqualTo(5);
    }

    // ===== Boost applies even if artifact is indestructible =====

    @Test
    @DisplayName("Boost applies even if target artifact is indestructible")
    void boostAppliesEvenIfIndestructible() {
        Permanent dragon = addDragon(player1);
        Permanent artifact = new Permanent(new RodOfRuin());
        artifact.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        // Artifact survives (indestructible)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Rod of Ruin"));

        // Dragon still gets the boost
        assertThat(dragon.getEffectivePower()).isEqualTo(9); // 5 + 4
    }

    // ===== Helpers =====

    private Permanent addDragon(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new HoardSmelterDragon());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
