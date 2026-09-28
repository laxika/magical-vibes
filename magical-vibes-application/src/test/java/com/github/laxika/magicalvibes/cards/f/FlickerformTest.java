package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CourierHawk;
import com.github.laxika.magicalvibes.cards.m.MoldervineCloak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flickerform.class, CourierHawk.class, MoldervineCloak.class})
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
        harness.passBothPriorities();
    }
}
