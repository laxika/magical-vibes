package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.m.MoldervineCloak;
import com.github.laxika.magicalvibes.cards.s.SvogthosTheRestlessTomb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flickerform.class, CourierHawk.class, MoldervineCloak.class, SvogthosTheRestlessTomb.class})
class FlickerformTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the enchanted creature and all attached Auras, then returns them attached at the next end step")
    void exilesAndReturnsEnchantedCreatureAndAuras() {
        Permanent creature = setupFlickerform();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Courier Hawk"))
                .anyMatch(card -> card.getName().equals("Flickerform"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Moldervine Cloak"));

        advanceToEndStep();

        Permanent returnedCreature = findPermanent(player1, "Courier Hawk");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Flickerform")
                        && returnedCreature.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Moldervine Cloak")
                        && returnedCreature.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(card -> card.getName().equals("Flickerform"));
        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getName().equals("Moldervine Cloak"));
        assertThat(creature.getId()).isNotEqualTo(returnedCreature.getId());
    }

    @Test
    @DisplayName("Returns every attached Aura under its owner's control")
    void returnsEveryAttachedAuraUnderItsOwnersControl() {
        Permanent creature = setupFlickerform();

        MoldervineCloak ownedByPlayer1 = new MoldervineCloak();
        ownedByPlayer1.setOwnerId(player1.getId());
        Permanent secondAura = new Permanent(ownedByPlayer1);
        secondAura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(secondAura);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        advanceToEndStep();

        Permanent returnedCreature = findPermanent(player1, "Courier Hawk");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Moldervine Cloak"))
                .hasSize(1)
                .allMatch(permanent -> returnedCreature.getId().equals(permanent.getAttachedTo()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Moldervine Cloak"))
                .hasSize(1)
                .allMatch(permanent -> returnedCreature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Leaves the other exiled cards in exile if the enchanted creature does not return")
    void doesNotReturnAurasWithoutTheCreature() {
        Permanent creature = setupFlickerform();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        gd.addCardToHand(player1.getId(), creature.getCard());
        gd.removeFromExile(creature.getCard().getId());

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Flickerform"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Moldervine Cloak"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Flickerform"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Moldervine Cloak"));
    }

    @Test
    @DisplayName("Auras remain exiled when the enchanted animated land returns as a noncreature")
    void aurasCannotReturnAttachedToAnUnanimatedLand() {
        harness.setGraveyard(player1, List.of(new CourierHawk()));
        Permanent land = addCreatureReady(player1, new SvogthosTheRestlessTomb());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent aura = new Permanent(new Flickerform());
        aura.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        Permanent cloak = new Permanent(new MoldervineCloak());
        cloak.setAttachedTo(land.getId());
        gd.playerBattlefields.get(player1.getId()).add(cloak);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Svogthos, the Restless Tomb");
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Svogthos, the Restless Tomb");
        harness.assertNotOnBattlefield(player1, "Flickerform");
        harness.assertNotOnBattlefield(player1, "Moldervine Cloak");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Flickerform"))
                .anyMatch(card -> card.getName().equals("Moldervine Cloak"));
        harness.assertNotInGraveyard(player1, "Flickerform");
        harness.assertNotInGraveyard(player1, "Moldervine Cloak");
    }

    @Test
    @DisplayName("The delayed return belongs to Flickerform's controller, even on an opponent's creature")
    void delayedReturnRetainsTheActivatedAbilityControllerAndSource() {
        Permanent creature = addCreatureReady(player2, new CourierHawk());
        creature.getCard().setOwnerId(player2.getId());
        Flickerform card = new Flickerform();
        card.setOwnerId(player1.getId());
        Permanent aura = new Permanent(card);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(card.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Courier Hawk");
        assertThat(findPermanent(player1, "Flickerform").getAttachedTo())
                .isEqualTo(findPermanent(player2, "Courier Hawk").getId());
    }

    private Permanent setupFlickerform() {
        Permanent creature = addCreatureReady(player1, new CourierHawk());

        Permanent flickerform = new Permanent(new Flickerform());
        flickerform.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(flickerform);

        MoldervineCloak moldervineCloak = new MoldervineCloak();
        moldervineCloak.setOwnerId(player2.getId());
        Permanent otherAura = new Permanent(moldervineCloak);
        otherAura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(otherAura);
        return creature;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
