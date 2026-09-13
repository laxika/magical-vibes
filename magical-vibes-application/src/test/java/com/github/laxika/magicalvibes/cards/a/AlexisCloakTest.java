package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChimericIdol;
import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.r.RhysticDeluge;
import com.github.laxika.magicalvibes.cards.w.Withdraw;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlexisCloak.class, ChimericIdol.class, DivingGriffin.class, RhysticDeluge.class, Withdraw.class})
class AlexisCloakTest extends BaseCardTest {

    @Test
    void resolvingAttachesAndGrantsShroud() {
        Permanent creature = addCreatureReady(player1, new DivingGriffin());
        harness.setHand(player1, List.of(new AlexisCloak()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    void shroudIsLostWhenAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new DivingGriffin());
        Permanent aura = attachAura(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    void shroudPreventsTargetingEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new DivingGriffin());
        Permanent otherCreature = addCreatureReady(player1, new DivingGriffin());
        attachAura(creature);
        harness.setHand(player1, List.of(new Withdraw()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), otherCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void shroudPreventsTargetingEnchantedCreatureByActivatedAbility() {
        Permanent deluge = harness.addToBattlefieldAndReturn(player1, new RhysticDeluge());
        Permanent creature = addCreatureReady(player1, new DivingGriffin());
        attachAura(creature);
        harness.addMana(player1, ManaColor.BLUE, 1);

        int delugeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(deluge);
        assertThatThrownBy(() -> harness.activateAbility(player1, delugeIndex, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void canCastDuringOpponentsTurnThanksToFlash() {
        Permanent creature = addCreatureReady(player1, new DivingGriffin());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AlexisCloak()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        gs.passPriority(gd, player2);
        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void fizzlesIfTargetCreatureLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new DivingGriffin());
        harness.setHand(player1, List.of(new AlexisCloak()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Alexi's Cloak");
        harness.assertNotOnBattlefield(player1, "Alexi's Cloak");
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ChimericIdol());
        harness.setHand(player1, List.of(new AlexisCloak()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AlexisCloak());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
