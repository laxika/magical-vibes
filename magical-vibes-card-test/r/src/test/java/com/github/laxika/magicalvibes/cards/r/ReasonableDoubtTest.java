package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.cards.t.TunnelTipster;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReasonableDoubt.class, TunnelTipster.class, NervousGardener.class, Island.class})
class ReasonableDoubtTest extends BaseCardTest {

    @Test
    void countersSpellAndSuspectsOptionalCreatureWhenControllerCannotPay() {
        Permanent creatureTarget = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        UUID creatureId = creatureTarget.getId();
        NervousGardener spell = new NervousGardener();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ReasonableDoubt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), creatureId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nervous Gardener");
        Permanent creature = creatureTarget;
        assertThat(creature.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, creature)).isFalse();
    }

    @Test
    void spellResolvesWhenControllerPaysTwo() {
        Permanent creatureTarget = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        UUID creatureId = creatureTarget.getId();
        NervousGardener spell = new NervousGardener();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new ReasonableDoubt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), creatureId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nervous Gardener");
        assertThat(creatureTarget.isSuspected()).isTrue();
    }

    @Test
    void resolvesWithoutChoosingOptionalCreatureTarget() {
        NervousGardener spell = new NervousGardener();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ReasonableDoubt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nervous Gardener");
    }

    @Test
    void cannotChooseNonCreatureForOptionalTarget() {
        UUID islandId = harness.addToBattlefieldAndReturn(player1, new Island()).getId();
        NervousGardener spell = new NervousGardener();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ReasonableDoubt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, spell.getId(), islandId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillSuspectsOwnCreatureWhenSpellTargetLeavesStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TunnelTipster());
        NervousGardener spell = new NervousGardener();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new ReasonableDoubt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), creature.getId());

        gd.stack.removeIf(entry -> entry.getCard().getId().equals(spell.getId()));
        harness.passBothPriorities();

        assertThat(creature.isSuspected()).isTrue();
        harness.assertInGraveyard(player2, "Reasonable Doubt");
    }

    @Test
    void stillCountersWhenCreatureTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        NervousGardener spell = new NervousGardener();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new ReasonableDoubt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nervous Gardener");
        harness.assertInGraveyard(player2, "Reasonable Doubt");
    }

    @Test
    void countersWhenControllerDeclinesPaymentAndStillSuspectsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TunnelTipster());
        NervousGardener spell = new NervousGardener();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new ReasonableDoubt()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId(), creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Nervous Gardener");
        assertThat(creature.isSuspected()).isTrue();
    }
}
