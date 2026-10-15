package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KarplusanYeti;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TorWaukiTheYounger.class, Divination.class, Shock.class, HillGiant.class, KarplusanYeti.class})
class TorWaukiTheYoungerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant or sorcery triggers 2 damage to any target")
    void castingInstantOrSorceryTriggersDamage() {
        addTorWauki();
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        chooseTriggerTarget(player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Another source's noncombat damage gets +1, but Tor Wauki's trigger does not")
    void boostsAnotherSourceDamageOnly() {
        addTorWauki();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        chooseTriggerTarget(player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("An opponent's noncombat damage is not increased")
    void doesNotBoostOpponentsSources() {
        addTorWauki();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The trigger can deal its damage to a permanent")
    void triggerCanTargetPermanent() {
        addTorWauki();
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new Divination()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        chooseTriggerTarget(target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tor Wauki's trigger gains life before the spell resolves")
    void triggerHasLifelinkAndResolvesBeforeSpell() {
        addTorWauki();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        chooseTriggerTarget(player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Another source's damage to your own creature is increased")
    void boostsDamageToOwnPermanent() {
        addTorWauki();
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        chooseTriggerTarget(player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Another source's damage to you is increased")
    void boostsDamageToOwnController() {
        addTorWauki();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        chooseTriggerTarget(player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Casting a creature does not trigger Tor Wauki")
    void creatureSpellDoesNotTrigger() {
        addTorWauki();
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Another creature's combat damage is not increased")
    void doesNotBoostCombatDamage() {
        addTorWauki();
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Mutual creature damage increases only your creature's damage")
    void mutualDamageDoesNotBoostOpposingCreature() {
        addTorWauki();
        Permanent yeti = addCreatureReady(player1, new KarplusanYeti());
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbility(player1, 1, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(yeti.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Your creature's return damage is increased during an opponent's ability")
    void mutualDamageBoostsOwnCreatureDuringOpponentsAbility() {
        addTorWauki();
        Permanent target = addCreatureReady(player1, new HillGiant());
        Permanent yeti = addCreatureReady(player2, new KarplusanYeti());
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(yeti.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Tor Wauki's trigger survives its source dying and retains lifelink")
    void triggerResolvesAfterSourceDies() {
        addTorWauki();
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        java.util.UUID torWaukiId = harness.getPermanentId(player1, "Tor Wauki the Younger");

        harness.castInstant(player1, 0, player2.getId());
        chooseTriggerTarget(player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, torWaukiId);
        harness.passBothPriorities();
        harness.passPriority(player1);
        harness.castInstant(player2, 0, torWaukiId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tor Wauki the Younger");
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 16);
    }

    private void addTorWauki() {
        harness.addToBattlefield(player1, new TorWaukiTheYounger());
    }

    private void chooseTriggerTarget(java.util.UUID targetId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
    }
}
