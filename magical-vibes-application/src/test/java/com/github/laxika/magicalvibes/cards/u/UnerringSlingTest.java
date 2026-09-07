package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnerringSling.class, GrizzlyBears.class, SuntailHawk.class, AirElemental.class})
class UnerringSlingTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the tapped creature's power to an attacking flier")
    void damagesAttackingFlier() {
        Permanent sling = addSlingReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent hawk = addCombatCreature(new SuntailHawk(), true, false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, hawk.getId());
        harness.passBothPriorities();

        assertThat(sling.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("A tougher blocking flier survives the damage")
    void blockingFlierSurvivesLesserDamage() {
        addSlingReady();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent elemental = addCombatCreature(new AirElemental(), false, true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, elemental.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(elemental.getId()));
        assertThat(elemental.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Uses the power of the creature chosen for the tap cost")
    void usesChosenCreaturePower() {
        addSlingReady();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        Permanent hawk = addCombatCreature(new SuntailHawk(), true, false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, hawk.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Cannot target an attacking creature without flying")
    void cannotTargetNonFlyingAttacker() {
        addSlingReady();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCombatCreature(new GrizzlyBears(), true, false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Cannot target a flier that is neither attacking nor blocking")
    void cannotTargetIdleFlier() {
        addSlingReady();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent idle = addCombatCreature(new SuntailHawk(), false, false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @CardUsed(DarkBanishing.class)
    @DisplayName("Uses the tapped creature's power if it leaves before resolution")
    void usesTappedCreaturePowerAfterItLeaves() {
        addSlingReady();
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        Permanent hawk = addCombatCreature(new SuntailHawk(), true, false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, hawk.getId());

        harness.setHand(player2, List.of(new DarkBanishing()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, elemental.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    private Permanent addSlingReady() {
        Permanent sling = new Permanent(new UnerringSling());
        sling.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sling);
        return sling;
    }

    private Permanent addCombatCreature(Card card, boolean attacking, boolean blocking) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        return creature;
    }
}
