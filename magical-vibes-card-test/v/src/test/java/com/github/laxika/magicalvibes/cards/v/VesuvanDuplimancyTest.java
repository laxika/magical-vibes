package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BashToBits;
import com.github.laxika.magicalvibes.cards.c.CombatResearch;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VesuvanDuplimancy.class, GiantGrowth.class, IsamaruHoundOfKonda.class,
        BashToBits.class, Spellbook.class, DualShot.class, GrizzlyBears.class,
        CombatResearch.class, Unsummon.class, RayOfCommand.class, LiquimetalCoating.class, SeedsOfStrength.class})
class VesuvanDuplimancyTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a single targeted creature without its legendary supertype")
    void createsNonlegendaryTokenCopyOfTargetedCreature() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        List<Permanent> tokens = tokenCopies(player1);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getSupertypes())
                .doesNotContain(CardSupertype.LEGENDARY);
    }

    @Test
    @DisplayName("Copies a single targeted artifact")
    void createsTokenCopyOfTargetedArtifact() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(1);
        assertThat(tokenCopies(player1).getFirst().getCard().getType())
                .isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Does not trigger for a spell with multiple targets")
    void doesNotTriggerForMultipleTargets() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DualShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.of(firstTarget.getId(), secondTarget.getId()));

        assertThat(tokenCopies(player1)).isEmpty();
        assertThat(firstTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for an artifact controlled by an opponent")
    void doesNotTriggerForOpponentsArtifact() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(tokenCopies(player1)).isEmpty();
    }

    @Test
    @DisplayName("Copies the original creature using last known information after it leaves")
    void copiesCreatureAfterItLeavesBattlefield() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(1);
        Permanent token = tokenCopies(player1).getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(tokenCopies(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Copies the original creature even after an opponent gains control of it")
    void copiesCreatureAfterControlChanges() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(1);
        assertThat(tokenCopies(player2)).isEmpty();
        harness.passBothPriorities();
        Permanent token = tokenCopies(player1).getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Aura spells trigger the ability before the Aura resolves")
    void triggersForAuraSpell() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CombatResearch()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> target.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> tokenCopies(player1).getFirst().getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("An opponent casting a spell on your creature does not trigger the ability")
    void doesNotTriggerForOpponentsSpell() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(tokenCopies(player1)).isEmpty();
        assertThat(tokenCopies(player2)).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can copy itself when it has become an artifact")
    void copiesItselfWhenArtifact() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new VesuvanDuplimancy());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.setHand(player1, List.of(new BashToBits()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, source.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, source.getId());

        assertThat(tokenCopies(player1)).hasSize(1);
        assertThat(tokenCopies(player1).getFirst().getCard().getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(tokenCopies(player1).getFirst().getCard().getAdditionalTypes()).doesNotContain(CardType.ARTIFACT);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(tokenCopies(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Triggers once when a spell targets the same creature several times")
    void triggersForRepeatedTargetingOfSameCreature() {
        harness.addToBattlefield(player1, new VesuvanDuplimancy());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeedsOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0,
                List.of(target.getId(), target.getId(), target.getId()));

        assertThat(tokenCopies(player1)).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, tokenCopies(player1).getFirst())).isEqualTo(2);
    }

    private List<Permanent> tokenCopies(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
