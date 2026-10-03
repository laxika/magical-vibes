package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Afterlife;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.r.RishadanPawnshop;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiplomaticImmunity.class, FreshVolunteers.class, Afterlife.class, Disenchant.class,
        RishadanPawnshop.class})
class DiplomaticImmunityTest extends BaseCardTest {

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        harness.setHand(player1, List.of(new DiplomaticImmunity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
    }

    @Test
    void grantsShroudToEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        attachAura(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    void shroudPreventsTargetingEnchantedCreatureAndAura() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        Permanent aura = attachAura(creature);

        harness.setHand(player1, List.of(new Afterlife()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shroudPreventsDiplomaticImmunityFromTargetingTheCreature() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        attachAura(creature);

        harness.setHand(player1, List.of(new DiplomaticImmunity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shroudEndsWhenAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        Permanent aura = attachAura(creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new RishadanPawnshop());
        harness.setHand(player1, List.of(new DiplomaticImmunity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DiplomaticImmunity());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    void canEnchantOpponentsCreatureAndRemainsAttachedDespiteShroud() {
        Permanent creature = addCreatureReady(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new DiplomaticImmunity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.runStateBasedActions();

        Permanent aura = findPermanent(player1, "Diplomatic Immunity");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        harness.setHand(player2, List.of(new Afterlife()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void shroudPreventsActivatedAbilityFromTargetingAura() {
        Permanent creature = addCreatureReady(player1, new FreshVolunteers());
        Permanent aura = attachAura(creature);
        harness.addToBattlefield(player1, new RishadanPawnshop());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
