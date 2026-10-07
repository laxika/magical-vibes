package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyrArmontTheRedeemer.class, GrizzlyBears.class, Pacifism.class})
class SyrArmontTheRedeemerTest extends BaseCardTest {

    @Test
    void createsMonsterRoleAttachedToAnotherCreatureYouControl() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        castSyrArmont(target);

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    void boostsAllEnchantedCreaturesYouControlIncludingSyrArmont() {
        Permanent syrArmont = addCreatureReady(player1, new SyrArmontTheRedeemer());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, syrArmont);
        attachAura(player2, ownCreature);
        attachAura(player2, opposingCreature);

        assertThat(gqs.getEffectivePower(gd, syrArmont)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, syrArmont)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    void cannotTargetAnOpponentCreature() {
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new SyrArmontTheRedeemer()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    void multipleAurasDoNotMultiplyTheBonusAndRemovingTheLastAuraEndsIt() {
        addCreatureReady(player1, new SyrArmontTheRedeemer());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);
        attachAura(player2, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, findPermanent(player1, "Pacifism"));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, findPermanent(player2, "Pacifism"));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void monsterRoleStillResolvesAfterSyrArmontLeavesTheBattlefield() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new SyrArmontTheRedeemer()));
        addMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd,
                findPermanent(player1, "Syr Armont, the Redeemer"));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void doesNotCreateARoleWhenTheTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new SyrArmontTheRedeemer()));
        addMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Monster")).isZero();
        harness.assertOnBattlefield(player1, "Syr Armont, the Redeemer");
    }

    @Test
    void newMonsterRoleReplacesAnOlderRoleControlledByTheSamePlayer() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castSyrArmont(target);
        Permanent oldRole = findPermanent(player1, "Monster");
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd,
                findPermanent(player1, "Syr Armont, the Redeemer"));
        castSyrArmont(target);

        assertThat(countPermanents(player1, "Monster")).isEqualTo(1);
        assertThat(findPermanent(player1, "Monster").getId()).isNotEqualTo(oldRole.getId());
        assertThat(findPermanent(player1, "Monster").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    private void castSyrArmont(Permanent target) {
        harness.setHand(player1, java.util.List.of(new SyrArmontTheRedeemer()));
        addMana();
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void attachAura(Player controller, Permanent host) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
