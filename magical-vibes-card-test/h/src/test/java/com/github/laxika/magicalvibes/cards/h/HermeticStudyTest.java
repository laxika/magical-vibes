package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HermeticStudy.class, FountainOfYouth.class, GrizzlyBears.class, LlanowarElves.class})
class HermeticStudyTest extends BaseCardTest {

    @Test
    void enchantedCreatureCanTapToDealDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent creature = addEnchantedCreature();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void enchantedCreatureCanTapToDealDamageToCreature() {
        addEnchantedCreature();
        Permanent target = addCreatureReady(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void summoningSickEnchantedCreatureCannotActivateGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addAura(creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void onlyEnchantedCreatureGetsGrantedAbility() {
        addEnchantedCreature();
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        int otherCreatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(otherCreature);

        assertThatThrownBy(() -> harness.activateAbility(player1, otherCreatureIndex, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    void enchantedCreatureControllerCanActivateGrantedAbility() {
        harness.setLife(player1, 20);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(creature.getId());

        int creatureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        harness.activateAbility(player2, creatureIndex, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void grantedAbilityStopsWhenAuraIsNoLongerAttached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAura(creature);
        aura.setAttachedTo(null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new HermeticStudy()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void castingAuraGrantsAbilityAndCreatureIsTheDamageSource() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HermeticStudy()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player2, 20);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.damageDealtThisTurnBySource.get(creature.getId())).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void tappedCreatureCannotPayTapCostAgain() {
        Permanent creature = addEnchantedCreature();
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedAbilityCanDamageItsOwnController() {
        harness.setLife(player1, 20);
        addEnchantedCreature();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void activatedAbilityStillResolvesAfterAuraLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAura(creature);
        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        creature.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent addEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addAura(creature);
        return creature;
    }

    private Permanent addAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
