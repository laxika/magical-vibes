package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({SyrArmontTheRedeemer.class, GrizzlyBears.class, Pacifism.class, Mountain.class})
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

    private void castSyrArmont(Permanent target) {
        harness.setHand(player1, java.util.List.of(new SyrArmontTheRedeemer()));
        addMana();
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
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
