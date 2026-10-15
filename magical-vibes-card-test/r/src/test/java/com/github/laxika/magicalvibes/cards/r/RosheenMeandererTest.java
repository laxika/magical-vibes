package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.c.Condescend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnollspineInvocation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RosheenMeanderer.class, Fireball.class, GrizzlyBears.class, KnollspineInvocation.class,
        Condescend.class})
class RosheenMeandererTest extends BaseCardTest {

    private void activateManaAbility() {
        Permanent rosheen = gd.playerBattlefields.get(player1.getId()).getFirst();
        rosheen.setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, null);
    }

    @Test
    @DisplayName("Tap ability adds four x-cost-only colorless mana")
    void tapAbilityAddsFourXCostOnlyColorless() {
        harness.addToBattlefield(player1, new RosheenMeanderer());

        activateManaAbility();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("X-cost-only mana pays the generic X portion of an X spell")
    void xCostOnlyManaPaysXSpell() {
        harness.addToBattlefield(player1, new RosheenMeanderer());
        activateManaAbility();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Fireball {X}{R}: X=4 → 4 generic (paid by x-cost-only) + {R} (paid by red).
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Fireball()));
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 4, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("X-cost-only mana cannot pay a spell whose cost contains no {X}")
    void xCostOnlyManaCannotPayNonXSpell() {
        harness.addToBattlefield(player1, new RosheenMeanderer());
        activateManaAbility();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Grizzly Bears {1}{G}: no {X} in cost, so the generic {1} can't come from x-cost-only mana.
        // Only 1 green available → not enough.
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(4);
    }

    @Test
    @DisplayName("X-cost-only mana drains at phase transition")
    void xCostOnlyManaDrainsAtPhaseTransition() {
        harness.addToBattlefield(player1, new RosheenMeanderer());
        activateManaAbility();
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(4);

        gd.playerManaPools.get(player1.getId()).drainNonPersistent();

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(0);
    }

    @Test
    void manaCanPayAnActivatedAbilityContainingX() {
        harness.addToBattlefield(player1, new RosheenMeanderer());
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new RosheenMeanderer()));
        harness.setLife(player2, 20);
        activateManaAbility();

        harness.activateAbility(player1, 1, 4, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
        harness.assertInGraveyard(player1, "Rosheen Meanderer");
    }

    @Test
    void manaCanPayAdditionalGenericCostWhenXIsZero() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RosheenMeanderer());
        activateManaAbility();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Fireball()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent rosheen = harness.addToBattlefieldAndReturn(player1, new RosheenMeanderer());
        rosheen.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
        assertThat(rosheen.isTapped()).isFalse();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        harness.addToBattlefield(player1, new RosheenMeanderer());
        activateManaAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(4);
    }

    @Test
    void manaDrainsWhenTheEngineAdvancesToCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RosheenMeanderer());
        activateManaAbility();

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
    }

    @Test
    void manaCanPayXDuringCondescendsResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new RosheenMeanderer());
        activateManaAbility();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Condescend()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 4, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(bears.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isZero();
    }
}
